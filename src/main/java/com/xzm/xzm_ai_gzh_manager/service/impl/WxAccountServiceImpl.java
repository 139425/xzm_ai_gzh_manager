package com.xzm.xzm_ai_gzh_manager.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.xzm.xzm_ai_gzh_manager.common.ErrorCode;
import com.xzm.xzm_ai_gzh_manager.common.PageRequest;
import com.xzm.xzm_ai_gzh_manager.exception.BusinessException;
import com.xzm.xzm_ai_gzh_manager.exception.ThrowUtils;
import com.xzm.xzm_ai_gzh_manager.mapper.WxAccountMapper;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountAddDTO;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountPageQueryDTO;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountUpdateDTO;
import com.xzm.xzm_ai_gzh_manager.model.entity.User;
import com.xzm.xzm_ai_gzh_manager.model.entity.WxAccount;
import com.xzm.xzm_ai_gzh_manager.model.vo.WxAccountVO;
import com.xzm.xzm_ai_gzh_manager.service.UserService;
import com.xzm.xzm_ai_gzh_manager.service.WxAccountService;

import com.xzm.xzm_ai_gzh_manager.utils.CopyUtil;
import com.xzm.xzm_ai_gzh_manager.utils.WrapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.lang.Console.log;

/**
* @author 34631
* @description 针对表【wx_account(微信公众号账号)】的数据库操作Service实现
* @createDate 2025-07-18 21:12:21
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class WxAccountServiceImpl extends ServiceImpl<WxAccountMapper, WxAccount>
implements WxAccountService {



    private final WxMpService wxMpService;

    private final UserService  userService;

    /**
     * 将公众号配置添加到运行时服务中
     * @param wxAccount 公众号信息
     */
    private synchronized void addAccountToRuntime(WxAccount wxAccount){
        String appid = wxAccount.getAppId();
        WxMpDefaultConfigImpl config =wxAccount.toWxMpConfigStorage();
        try{
            wxMpService.addConfigStorage(appid,config);
        }catch (NullPointerException  e){
            // 当 wxMpService 最开始没有公众号配置时可能会出现空指针异常
            log("初始化configStorageMap...");
            Map<String, WxMpConfigStorage> configStorages = new HashMap<>(6);
            configStorages.put(appid, config);
            wxMpService.setMultiConfigStorages(configStorages, appid);
        }
    }
    private boolean isAccountInRuntime(String appid){
        try{
            return wxMpService.switchover(appid);
        }catch (NullPointerException e){
            return false;
        }
    }


    /**
     * 保存公众号信息并添加到运行时服务
     * @param wxAccountAddDTO 新增公众号的数据传输对象
     * @param userId 用户ID
     * @return 新增公众号的ID
     */
    public Long saveAndToRuntime(WxAccountAddDTO wxAccountAddDTO, Long userId) {
        // 检查公众号是否已存在于数据库
        ThrowUtils.throwIf(
                this.count(Wrappers.lambdaQuery(WxAccount.class).eq(WxAccount::getAppId, wxAccountAddDTO.getAppId())) != 0,
                ErrorCode.PARAMS_ERROR,
                "公众号已存在"
        );

        String appId = wxAccountAddDTO.getAppId();
        // 使用字符串对象的intern方法获取字符串池中的对象，确保锁定的是同一个对象
        synchronized (appId.intern()) {
            // 检查公众号是否已存在于运行时
            if (this.isAccountInRuntime(appId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "公众号已存在");
            }

            // 复制DTO到实体对象
            WxAccount wxAccount = CopyUtil.copy(wxAccountAddDTO, WxAccount.class);
            wxAccount.setUserId(userId);

            // 保存到数据库
            this.save(wxAccount);

            // 添加到wxJava运行时
            this.addAccountToRuntime(wxAccount);
            return wxAccount.getId();
        }
    }

    public Boolean deleteByAppids (List<String> appids){
        //先删除Wxjava里的数据
        appids.forEach(wxMpService::removeConfigStorage);
        //再删除数据库里的数据

        return this.remove(Wrappers.lambdaQuery(WxAccount.class)
                .in(WxAccount::getAppId, appids));
    }

    /**
     * 更新公众号信息并更新运行时配置
     * @param wxAccountUpdateDTO 更新的公众号信息
     * @return 更新是否成功
     */
    public Boolean updateAndToRuntime(WxAccountUpdateDTO wxAccountUpdateDTO){
        //获取数据库中的公众号信息
        WxAccount wxAccountDb = this.getById(wxAccountUpdateDTO.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxAccountDb), ErrorCode.PARAMS_ERROR, "公众号不存在");

        String oldAppId = wxAccountDb.getAppId();
        //检查运行时是否存在该公众号

        if(!this.isAccountInRuntime(oldAppId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公众号不存在");
        }

        //如果appid为空，则使用原来的appid
        if(StringUtils.isBlank(wxAccountUpdateDTO.getAppId())){
            wxAccountUpdateDTO.setAppId(oldAppId);
        }

        // 复制更新数据到实体
        WxAccount wxAccount = CopyUtil.copy(wxAccountUpdateDTO, WxAccount.class);

        // 更新数据库
        boolean result = SqlHelper.retBool(this.baseMapper.updateById(wxAccount));

        // 先移除旧配置
        wxMpService.removeConfigStorage(oldAppId);
        // 删除后再添加到 wxJava（这里再查一遍目的是拿到最新的微信公众号信息，
        // 后面如果又加了字段，不这样做可能会漏掉字段）
        this.addAccountToRuntime(this.getById(wxAccountUpdateDTO.getId()));

        return result;
    }

    /**
     * 构建查询条件
     * @param wxAccountPageQueryDTO 查询参数
     * @return 查询条件包装器
     */
    public QueryWrapper<WxAccount> getQueryWrapper(WxAccountPageQueryDTO wxAccountPageQueryDTO) {
        if (wxAccountPageQueryDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        // 提取查询参数
        String appId = wxAccountPageQueryDTO.getAppId();
        String name = wxAccountPageQueryDTO.getName();
        Boolean verified = wxAccountPageQueryDTO.getVerified();
        String secret = wxAccountPageQueryDTO.getSecret();
        String token = wxAccountPageQueryDTO.getToken();
        String aesKey = wxAccountPageQueryDTO.getAesKey();
        String sortField = wxAccountPageQueryDTO.getSortField();
        String sortOrder = wxAccountPageQueryDTO.getSortOrder();
        List<PageRequest.Sorter> sorterList = wxAccountPageQueryDTO.getSorterList();

        // 构建查询条件
        QueryWrapper<WxAccount> queryWrapper = Wrappers.query();
        queryWrapper.eq(StringUtils.isNotBlank(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(verified), "verified", verified);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(secret), "secret", secret);
        queryWrapper.like(StringUtils.isNotBlank(token), "userName", token);
        queryWrapper.like(StringUtils.isNotBlank(aesKey), "aesKey", aesKey);

        // 处理排序
        WrapperUtil.handleOrder(queryWrapper, sorterList, sortField, sortOrder);
        return queryWrapper;
    }


   // @Override
    public Page<WxAccountVO> getPage(Page<WxAccount> wxAccountPage, QueryWrapper<WxAccount> wxAccountQueryWrapper) {
        Page<WxAccount> accountPage = this.page(wxAccountPage, wxAccountQueryWrapper);
        Page<WxAccountVO> pageResult = new Page<>();
        BeanUtils.copyProperties(accountPage, pageResult, "records");

        List<WxAccount> accountList = accountPage.getRecords();

        Set<Long> userIdSet = accountList.stream().map(WxAccount::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap;
        if (ObjectUtils.isNotEmpty(userIdSet)) {
            userIdUserListMap = userService.listByIds(userIdSet)
                    .stream()
                    .collect(Collectors.groupingBy(User::getId));
        } else {
            userIdUserListMap = new HashMap<>();
        }

        // 转换为VO
        pageResult.setRecords(
                accountList
                        .stream()
                        .map(wxAccount -> {
                            WxAccountVO wxAccountVO = CopyUtil.copy(wxAccount, WxAccountVO.class);
                            List<User> userList = userIdUserListMap.get(wxAccount.getUserId());
                            if (ObjectUtils.isNotEmpty(userList)) {
                                wxAccountVO.setUser(userService.getUserVO(userList.getFirst()));
                            }
                            return wxAccountVO;
                        })
                        .collect(Collectors.toList())
        );

        return pageResult;
    }

}
