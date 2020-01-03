package com.xuxueli.executor.sample.frameless.config;

import com.xuxueli.executor.sample.frameless.jobhandler.CommandJobHandler;
import com.xuxueli.executor.sample.frameless.jobhandler.DemoJobHandler;
import com.xuxueli.executor.sample.frameless.jobhandler.HttpJobHandler;
import com.xuxueli.executor.sample.frameless.jobhandler.ShardingJobHandler;
import com.xxl.job.core.executor.XxlJobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author xuxueli 2018-10-31 19:05:43
 */
public class FrameLessXxlJobConfig {
    private static Logger logger = LoggerFactory.getLogger(FrameLessXxlJobConfig.class);


    private static FrameLessXxlJobConfig instance = new FrameLessXxlJobConfig();
    public static FrameLessXxlJobConfig getInstance() {
        return instance;
    }


    private XxlJobExecutor xxlJobExecutor = null;

    /**
     * init
     */
    public void initXxlJobExecutor() {

        // registry jobhandler
        XxlJobExecutor.registJobHandler("demoJobHandler", new DemoJobHandler());
        XxlJobExecutor.registJobHandler("shardingJobHandler", new ShardingJobHandler());
        XxlJobExecutor.registJobHandler("httpJobHandler", new HttpJobHandler());
        XxlJobExecutor.registJobHandler("commandJobHandler", new CommandJobHandler());

        // load executor prop
        Properties xxlJobProp = loadProperties("xxl-job-executor.properties");


        // init executor
        xxlJobExecutor = new XxlJobExecutor();
        // 设置调度平台的地址，用于注册本Executor至调度平台且通过该地址回调结果到调度平台
        xxlJobExecutor.setAdminAddresses(xxlJobProp.getProperty("xxl.job.admin.addresses"));
        // 设置执行器名称
        xxlJobExecutor.setAppName(xxlJobProp.getProperty("xxl.job.executor.appname"));
        // 设置执行器的ip，用于注册到调度中心，调度中心通过该IP来调度执行器来执行任务
        xxlJobExecutor.setIp(xxlJobProp.getProperty("xxl.job.executor.ip"));
        // 设置执行器的端口，用于注册到调度中心，调度中心通过该port来调度执行器执行任务，需要在执行器开启端口服务
        xxlJobExecutor.setPort(Integer.valueOf(xxlJobProp.getProperty("xxl.job.executor.port")));
        // 注册到调度平台的token，如果调度平台开启token认证，则需要token，否则不需要
        xxlJobExecutor.setAccessToken(xxlJobProp.getProperty("xxl.job.accessToken"));
        // 执行器日志路径
        xxlJobExecutor.setLogPath(xxlJobProp.getProperty("xxl.job.executor.logpath"));
        xxlJobExecutor.setLogRetentionDays(Integer.valueOf(xxlJobProp.getProperty("xxl.job.executor.logretentiondays")));

        // start executor
        try {
            // 执行器启动服务监听端口用于接收调度平台的调度请求
            xxlJobExecutor.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * destory
     */
    public void destoryXxlJobExecutor() {
        if (xxlJobExecutor != null) {
            xxlJobExecutor.destroy();
        }
    }


    public static Properties loadProperties(String propertyFileName) {
        InputStreamReader in = null;
        try {
            ClassLoader loder = Thread.currentThread().getContextClassLoader();

            in = new InputStreamReader(loder.getResourceAsStream(propertyFileName), "UTF-8");;
            if (in != null) {
                Properties prop = new Properties();
                prop.load(in);
                return prop;
            }
        } catch (IOException e) {
            logger.error("load {} error!", propertyFileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close {} error!", propertyFileName);
                }
            }
        }
        return null;
    }

}
