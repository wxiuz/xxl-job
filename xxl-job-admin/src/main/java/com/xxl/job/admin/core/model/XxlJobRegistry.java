package com.xxl.job.admin.core.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by xuxueli on 16/9/30.
 */
@Data
public class XxlJobRegistry {

    private int id;

    // 当前注册属于哪种类型：EXECUTOR-执行器，ADMIN-???
    private String registryGroup;

    // 执行器所属应用名称
    private String registryKey;

    // 执行器地址
    private String registryValue;

    // 执行器定时保持心跳更新时间，当执行器发送心跳，此时会更新该字段
    private Date updateTime;
}
