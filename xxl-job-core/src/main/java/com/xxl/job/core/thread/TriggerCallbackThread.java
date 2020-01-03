package com.xxl.job.core.thread;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/7/22.
 */
public class TriggerCallbackThread {
    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);

    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    /**
     * job调用结果回调队列，所有的job执行结果都放到该队列，然后由专门线程来取该结果发送给调度中心
     */
    private LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<HandleCallbackParam>();

    /**
     * 处理callBackQueue数据的线程
     */
    private Thread triggerCallbackThread;

    private Thread triggerRetryCallbackThread;

    /**
     * 线程终止标志
     */
    private volatile boolean toStop = false;

    private TriggerCallbackThread(){}

    public static TriggerCallbackThread getInstance() {
        return instance;
    }

    /**
     * 将job调用结果放到结果回调队列中
     *
     * @param callback
     */
    public static void pushCallBack(HandleCallbackParam callback) {
        getInstance().callBackQueue.add(callback);
        logger.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    /**
     * 启动处理job结果的回调线程
     *
     */
    public void start() {
        // 先检查是否有调度中心客户端，没有则不作任何处理
        if (XxlJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }

        // callback
        triggerCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // normal callback
                while (!toStop) {
                    try {
                        // 从结果队列中获取一个结果，该方法会一直阻塞到有元素可以获取，主要通过该方法来完成producer-consumer模式，
                        // 从而在没有结果的时候该线程保持阻塞
                        HandleCallbackParam callback = getInstance().callBackQueue.take();
                        if (callback != null) {

                            // callback list
                            List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                            // 如果队列中有job调用结果，此时获取所有的调用结果并进行回调
                            getInstance().callBackQueue.drainTo(callbackParamList);
                            // 将第一个取出的数据一起加入到集合中
                            callbackParamList.add(callback);
                            // 回调操作
                            doCallback(callbackParamList);
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                try {
                    // 如果停止了当前线程，则将最后一次的结果进行回调给调度中心
                    List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                    getInstance().callBackQueue.drainTo(callbackParamList);
                    if (callbackParamList != null && callbackParamList.size() > 0) {
                        doCallback(callbackParamList);
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor callback thread destory.");

            }
        });
        triggerCallbackThread.setDaemon(true);
        triggerCallbackThread.setName("xxl-job, executor TriggerCallbackThread");
        triggerCallbackThread.start();


        // 重试发送job调用结果线程
        triggerRetryCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStop) {
                    try {
                        retryFailCallbackFile();
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }

                    }
                    try {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor retry callback thread destory.");
            }
        });
        triggerRetryCallbackThread.setDaemon(true);
        triggerRetryCallbackThread.start();

    }

    public void toStop() {
        toStop = true;
        // stop callback, interrupt and wait
        if (triggerCallbackThread != null) {    // support empty admin address
            triggerCallbackThread.interrupt();
            try {
                triggerCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // stop retry, interrupt and wait
        if (triggerRetryCallbackThread != null) {
            triggerRetryCallbackThread.interrupt();
            try {
                triggerRetryCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    /**
     * 将job调用结果发送给调度中心，如果发送失败，则将记录放到本地日志文件，通过重试线程来进行重试发送
     *
     * @param callbackParamList
     */
    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        boolean callbackRet = false;
        // 循环所有可用的调度中心，如果只要一个调度中心处理成功，则结束
        for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
            try {
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    callbackRet = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    /**
     * callback log
     */
    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent) {
        for (HandleCallbackParam callbackParam : callbackParamList) {
            String logFileName = XxlJobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTim()), callbackParam.getLogId());
            XxlJobFileAppender.contextHolder.set(logFileName);
            XxlJobLogger.log(logContent);
        }
    }


    // ---------------------- fail-callback file ----------------------

    private static String failCallbackFilePath = XxlJobFileAppender.getLogPath().concat(File.separator).concat("callbacklog").concat(File.separator);
    private static String failCallbackFileName = failCallbackFilePath.concat("xxl-job-callback-{x}").concat(".log");

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList) {
        // valid
        if (callbackParamList == null || callbackParamList.size() == 0) {
            return;
        }

        // append file
        byte[] callbackParamList_bytes = XxlJobExecutor.getSerializer().serialize(callbackParamList);

        File callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis())));
        if (callbackLogFile.exists()) {
            for (int i = 0; i < 100; i++) {
                callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis()).concat("-").concat(String.valueOf(i))));
                if (!callbackLogFile.exists()) {
                    break;
                }
            }
        }
        FileUtil.writeFileContent(callbackLogFile, callbackParamList_bytes);
    }

    private void retryFailCallbackFile() {

        // valid
        File callbackLogPath = new File(failCallbackFilePath);
        if (!callbackLogPath.exists()) {
            return;
        }
        if (callbackLogPath.isFile()) {
            callbackLogPath.delete();
        }
        if (!(callbackLogPath.isDirectory() && callbackLogPath.list() != null && callbackLogPath.list().length > 0)) {
            return;
        }

        // load and clear file, retry
        for (File callbaclLogFile : callbackLogPath.listFiles()) {
            byte[] callbackParamList_bytes = FileUtil.readFileContent(callbaclLogFile);
            List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) XxlJobExecutor.getSerializer().deserialize(callbackParamList_bytes, HandleCallbackParam.class);

            callbaclLogFile.delete();
            doCallback(callbackParamList);
        }

    }

}
