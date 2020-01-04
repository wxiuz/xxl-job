package com.xxl.job.admin.core.model;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 执行器model
 * Created by xuxueli on 16/9/30.
 */
@Data
public class XxlJobGroup {
    // 执行器ID
    private int id;

    // 执行器所属应用名称
    private String appName;

    // 执行器名称
    private String title;

    // 顺序
    private int order;

    // 执行器地址类型：0=自动注册、1=手动录入
    private int addressType;

    // 执行器地址列表，多地址逗号分隔
    private String addressList;

    // 执行器地址列表(系统注册)
    private List<String> registryList;

    public List<String> getRegistryList() {
        if (StringUtils.hasText(addressList)) {
            registryList = Lists.newArrayList(StringUtils.commaDelimitedListToSet(addressList));
        }
        return registryList;
    }
}
