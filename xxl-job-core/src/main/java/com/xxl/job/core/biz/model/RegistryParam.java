package com.xxl.job.core.biz.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xuxueli on 2017-05-10 20:22:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistryParam implements Serializable {
    private static final long serialVersionUID = 42L;

    // 注册类型：EXECUTOR-执行器，ADMIN-???
    private String registryGroup;

    // 执行器应用名称
    private String registryKey;

    // 执行器地址
    private String registryValue;
}
