package com.xxl.job.core.biz.model;

import java.io.Serializable;

/**
 * Created by xuxueli on 16/7/22.
 */
public class TriggerParam implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * jobId，存在调度中心的xxl_job_info的记录id
     */
    private int jobId;

    /**
     * 执行处理器，当glueType为为BEAN时，此参数为执行器端的注册到XxlJobExecutor中的IJobHandler处理器名称
     */
    private String executorHandler;

    /**
     * 执行任务时的参数
     */
    private String executorParams;

    /**
     * 当前job执行的阻塞策略，因为一个job在执行器端只有一个对应的线程来处理，如果上一个job还没执行完成，
     * 但是新的job又发起调度，此时需要配置相关处理策略，丢弃该次调度，覆盖原来调度【并且终止原来调度】，
     * 排队等待执行
     */
    private String executorBlockStrategy;

    /**
     * 任务执行超时时长
     */
    private int executorTimeout;

    /**
     * 调度中心执行时日志ID
     */
    private long logId;
    private long logDateTime;

    /**
     * 当前job类型，执行器会根据job类型来进行不同的处理
     */
    private String glueType;
    /**
     * 如果glueType为Bean时，此时改字段为空，否则该字段存储job执行代码
     */
    private String glueSource;
    private long glueUpdatetime;

    /**
     * 分片参数：当前机器得到的分片
     */
    private int broadcastIndex;
    /**
     * 分片参数：当前Job的总分片数，分片总数与存活的执行器有关，XXL-Job按照执行器的数量来进行自动分片
     */
    private int broadcastTotal;


    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getExecutorHandler() {
        return executorHandler;
    }

    public void setExecutorHandler(String executorHandler) {
        this.executorHandler = executorHandler;
    }

    public String getExecutorParams() {
        return executorParams;
    }

    public void setExecutorParams(String executorParams) {
        this.executorParams = executorParams;
    }

    public String getExecutorBlockStrategy() {
        return executorBlockStrategy;
    }

    public void setExecutorBlockStrategy(String executorBlockStrategy) {
        this.executorBlockStrategy = executorBlockStrategy;
    }

    public int getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(int executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(long logDateTime) {
        this.logDateTime = logDateTime;
    }

    public String getGlueType() {
        return glueType;
    }

    public void setGlueType(String glueType) {
        this.glueType = glueType;
    }

    public String getGlueSource() {
        return glueSource;
    }

    public void setGlueSource(String glueSource) {
        this.glueSource = glueSource;
    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    public void setGlueUpdatetime(long glueUpdatetime) {
        this.glueUpdatetime = glueUpdatetime;
    }

    public int getBroadcastIndex() {
        return broadcastIndex;
    }

    public void setBroadcastIndex(int broadcastIndex) {
        this.broadcastIndex = broadcastIndex;
    }

    public int getBroadcastTotal() {
        return broadcastTotal;
    }

    public void setBroadcastTotal(int broadcastTotal) {
        this.broadcastTotal = broadcastTotal;
    }


    @Override
    public String toString() {
        return "TriggerParam{" +
                "jobId=" + jobId +
                ", executorHandler='" + executorHandler + '\'' +
                ", executorParams='" + executorParams + '\'' +
                ", executorBlockStrategy='" + executorBlockStrategy + '\'' +
                ", executorTimeout=" + executorTimeout +
                ", logId=" + logId +
                ", logDateTime=" + logDateTime +
                ", glueType='" + glueType + '\'' +
                ", glueSource='" + glueSource + '\'' +
                ", glueUpdatetime=" + glueUpdatetime +
                ", broadcastIndex=" + broadcastIndex +
                ", broadcastTotal=" + broadcastTotal +
                '}';
    }

}
