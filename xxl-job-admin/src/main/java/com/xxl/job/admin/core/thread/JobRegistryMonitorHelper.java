package com.xxl.job.admin.core.thread;

import com.google.common.collect.TreeMultimap;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.core.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 将执行器注册信息同步至xxl_job_group表中
 *
 * @author xuxueli 2016-10-02 19:10:24
 */
public class JobRegistryMonitorHelper {
    private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);

    private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();

    public static JobRegistryMonitorHelper getInstance() {
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;

    public void start() {
        registryThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // 查询注册方式为自动注册的执行器：address type -->  0-自动注册，1-手动录入
                    List<XxlJobGroup> groupList = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().findByAddressType(0);
                    if (!CollectionUtils.isEmpty(groupList)) {
                        // 删除90s没有心跳的执行器
                        List<Integer> ids = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().findDead(RegistryConfig.DEAD_TIMEOUT, new Date());
                        if (ids != null && ids.size() > 0) {
                            XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().removeDead(ids);
                        }

                        final TreeMultimap<String, String> appAddressMap = TreeMultimap.create();
                        // 查询所有存活的执行器
                        List<XxlJobRegistry> list = XxlJobAdminConfig.getAdminConfig().getXxlJobRegistryDao().findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
                        if (!CollectionUtils.isEmpty(list)) {
                            list.stream().filter(x -> RegistryConfig.RegistType.EXECUTOR.name().equals(x.getRegistryGroup()))
                                    .forEach(x -> appAddressMap.put(x.getRegistryKey(), x.getRegistryValue()));
                        }

                        // 将自动注册的执行器地址信息更新到xxl_job_group中
                        for (XxlJobGroup group : groupList) {
                            // 获取当前执行器对应的地址信息
                            Set<String> registryList = appAddressMap.get(group.getAppName());
                            String addressListStr = null;
                            // 如果存在地址信息，则将地址信息更新到xxl_job_group中
                            if (!CollectionUtils.isEmpty(registryList)) {
                                addressListStr = StringUtils.collectionToCommaDelimitedString(registryList);
                            }
                            group.setAddressList(addressListStr);
                            XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().update(group);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, job registry monitor thread stop");
        });


        registryThread.setDaemon(true);
        registryThread.setName("xxl-job, admin JobRegistryMonitorHelper");
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
