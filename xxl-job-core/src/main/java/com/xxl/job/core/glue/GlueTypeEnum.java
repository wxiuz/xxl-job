package com.xxl.job.core.glue;

/**
 * Created by xuxueli on 17/4/26.
 */
public enum GlueTypeEnum {
    /**
     * 运行的业务代码在执行器端开发并直接放到执行器中
     */
    BEAN("BEAN", false, null, null),
    /**
     * 运行的业务代码放在调度中心的配置库中
     */
    GLUE_GROOVY("GLUE(Java)", false, null, null),
    /**
     * 运行的业务代码放在调度中心的配置库中
     */
    GLUE_SHELL("GLUE(Shell)", true, "bash", ".sh"),
    /**
     * 运行的业务代码放在调度中心的配置库中
     */
    GLUE_PYTHON("GLUE(Python)", true, "python", ".py"),
    /**
     * 运行的业务代码放在调度中心的配置库中
     */
    GLUE_PHP("GLUE(PHP)", true, "php", ".php"),
    /**
     * 运行的业务代码放在调度中心的配置库中
     */
    GLUE_NODEJS("GLUE(Nodejs)", true, "node", ".js"),
    /**
     * 运行的业务代码放在调度中心的配置库中
     */
    GLUE_POWERSHELL("GLUE(PowerShell)", true, "powershell", ".ps1");

    private String desc;
    private boolean isScript;
    private String cmd;
    private String suffix;

    GlueTypeEnum(String desc, boolean isScript, String cmd, String suffix) {
        this.desc = desc;
        this.isScript = isScript;
        this.cmd = cmd;
        this.suffix = suffix;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isScript() {
        return isScript;
    }

    public String getCmd() {
        return cmd;
    }

    public String getSuffix() {
        return suffix;
    }

    public static GlueTypeEnum match(String name) {
        for (GlueTypeEnum item : GlueTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return null;
    }

}
