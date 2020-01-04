package com.xxl.job.admin.controller;

import com.google.common.collect.TreeMultimap;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 执行器管理
 *
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

    @Resource
    public XxlJobInfoDao xxlJobInfoDao;
    @Resource
    public XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobRegistryDao xxlJobRegistryDao;

    @RequestMapping
    public String index(Model model) {
        // job group (executor)
        List<XxlJobGroup> list = xxlJobGroupDao.findAll();

        model.addAttribute("list", list);
        return "jobgroup/jobgroup.index";
    }

    /**
     * 添加执行器
     *
     * @param xxlJobGroup
     * @return
     */
    @RequestMapping("/save")
    @ResponseBody
    public ReturnT<String> save(XxlJobGroup xxlJobGroup) {

        // valid
        if (xxlJobGroup.getAppName() == null || xxlJobGroup.getAppName().trim().length() == 0) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
        }
        if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appName_length"));
        }
        if (xxlJobGroup.getTitle() == null || xxlJobGroup.getTitle().trim().length() == 0) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
        }
        if (xxlJobGroup.getAddressType() != 0) {
            if (xxlJobGroup.getAddressList() == null || xxlJobGroup.getAddressList().trim().length() == 0) {
                return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
            }
            String[] addresss = xxlJobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (item == null || item.trim().length() == 0) {
                    return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid"));
                }
            }
        }

        int ret = xxlJobGroupDao.save(xxlJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    /**
     * 更新执行器
     *
     * @param xxlJobGroup
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(XxlJobGroup xxlJobGroup) {
        // valid
        if (xxlJobGroup.getAppName() == null || xxlJobGroup.getAppName().trim().length() == 0) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + "AppName"));
        }
        if (xxlJobGroup.getAppName().length() < 4 || xxlJobGroup.getAppName().length() > 64) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_appName_length"));
        }
        if (xxlJobGroup.getTitle() == null || xxlJobGroup.getTitle().trim().length() == 0) {
            return new ReturnT<String>(500, (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")));
        }
        if (xxlJobGroup.getAddressType() == 0) {
            // 0=自动注册
            Set<String> registrySet = findRegistryByAppName(xxlJobGroup.getAppName());
            String addressListStr = null;
            if (!CollectionUtils.isEmpty(registrySet)) {
                addressListStr = StringUtils.collectionToCommaDelimitedString(registrySet);
            }
            xxlJobGroup.setAddressList(addressListStr);
        } else {
            // 1=手动录入
            if (xxlJobGroup.getAddressList() == null || xxlJobGroup.getAddressList().trim().length() == 0) {
                return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_addressType_limit"));
            }
            String[] addresss = xxlJobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (item == null || item.trim().length() == 0) {
                    return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid"));
                }
            }
        }

        int ret = xxlJobGroupDao.update(xxlJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    private Set<String> findRegistryByAppName(String appNameParam) {
        // 所有存活的执行器
        final List<XxlJobRegistry> list = xxlJobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
        final TreeMultimap<String, String> appAddressMap = TreeMultimap.create();
        if (!CollectionUtils.isEmpty(list)) {
            list.stream().filter(x -> RegistryConfig.RegistType.EXECUTOR.name().equals(x.getRegistryGroup()))
                    .forEach(x -> appAddressMap.put(x.getRegistryKey(), x.getRegistryValue()));
        }
        return appAddressMap.get(appNameParam);
    }

    /**
     * 删除执行器
     *
     * @param id
     * @return
     */
    @RequestMapping("/remove")
    @ResponseBody
    public ReturnT<String> remove(int id) {

        // valid
        int count = xxlJobInfoDao.pageListCount(0, 10, id, -1, null, null, null);
        if (count > 0) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_0"));
        }

        List<XxlJobGroup> allList = xxlJobGroupDao.findAll();
        if (allList.size() == 1) {
            return new ReturnT<String>(500, I18nUtil.getString("jobgroup_del_limit_1"));
        }

        int ret = xxlJobGroupDao.remove(id);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    /**
     * 根据ID查询执行器
     *
     * @param id
     * @return
     */
    @RequestMapping("/loadById")
    @ResponseBody
    public ReturnT<XxlJobGroup> loadById(int id) {
        XxlJobGroup jobGroup = xxlJobGroupDao.load(id);
        return jobGroup != null ? new ReturnT<XxlJobGroup>(jobGroup) : new ReturnT<XxlJobGroup>(ReturnT.FAIL_CODE, null);
    }

}
