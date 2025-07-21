package com.xzm.xzm_ai_gzh_manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountAddDTO;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountPageQueryDTO;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountUpdateDTO;
import com.xzm.xzm_ai_gzh_manager.model.entity.WxAccount;
import com.xzm.xzm_ai_gzh_manager.model.vo.WxAccountVO;

import java.util.List;


/**
 * @author 34631
 * @description 针对表【wx_account(微信公众号账号)】的数据库操作Service
 * @createDate 2025-07-18    21:12:21
 */
public interface WxAccountService extends IService<WxAccount> {
    // void addAccountToRuntime(WxAccount wxAccount);
    Long saveAndToRuntime(WxAccountAddDTO wxAccountAddDTO, Long userId);

    Boolean deleteByAppids(List<String> appids);

    Boolean updateAndToRuntime(WxAccountUpdateDTO wxAccountUpdateDTO);

    QueryWrapper<WxAccount> getQueryWrapper(WxAccountPageQueryDTO wxAccountPageQueryDTO);

    public Page<WxAccountVO> getPage(Page<WxAccount> wxAccountPage, QueryWrapper<WxAccount> wxAccountQueryWrapper);

    }
