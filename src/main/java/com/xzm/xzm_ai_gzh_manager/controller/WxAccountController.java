package com.xzm.xzm_ai_gzh_manager.controller;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzm.xzm_ai_gzh_manager.common.BaseResponse;
import com.xzm.xzm_ai_gzh_manager.common.ErrorCode;
import com.xzm.xzm_ai_gzh_manager.common.ResultUtils;
import com.xzm.xzm_ai_gzh_manager.exception.BusinessException;
import com.xzm.xzm_ai_gzh_manager.exception.ThrowUtils;


import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountAddDTO;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountPageQueryDTO;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpaccount.WxAccountUpdateDTO;
import com.xzm.xzm_ai_gzh_manager.model.entity.User;
import com.xzm.xzm_ai_gzh_manager.model.entity.WxAccount;
import com.xzm.xzm_ai_gzh_manager.model.vo.WxAccountVO;
import com.xzm.xzm_ai_gzh_manager.service.UserService;
import com.xzm.xzm_ai_gzh_manager.service.WxAccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wx/account")
@Slf4j
public class WxAccountController {
    private final WxAccountService wxAccountService;

    private final UserService userService;

    private final WxMpService wxMpService;

    @PostMapping("/add")
    @Operation(summary = "新增信息")
    public BaseResponse<Long> addWxMpAccount(@RequestBody WxAccountAddDTO wxAccountAddDTO, HttpServletRequest request) {
        // 参数校验
        if (ObjectUtils.isEmpty(wxAccountAddDTO)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getAppId()), ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getName()), ErrorCode.PARAMS_ERROR, "公众号名称不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getSecret()), ErrorCode.PARAMS_ERROR, "秘钥不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getToken()), ErrorCode.PARAMS_ERROR, "token不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(wxAccountAddDTO.getAesKey()), ErrorCode.PARAMS_ERROR, "aesKey不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务保存数据并返回结果
        return ResultUtils.success(wxAccountService.saveAndToRuntime(wxAccountAddDTO, loginUser.getId()));
    }

    /**
     * 删除公众号
     * @param appIds 要删除的公众号appId列表
     * @return 删除结果
     */
    @PostMapping("/delete")
    @Operation(summary = "根据appId删除列表")
    public BaseResponse<Boolean> deleteWxMpAccount(@RequestBody List<String> appIds) {
        return ResultUtils.success(wxAccountService.deleteByAppids(appIds));
    }


    /**
     * 更新公众号信息
     * @param wxAccountUpdateDTO 更新的公众号信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新公众号信息")
    public BaseResponse<Boolean> updateWxMpAccount(@RequestBody WxAccountUpdateDTO wxAccountUpdateDTO) {
        if (ObjectUtils.isEmpty(wxAccountUpdateDTO)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxAccountUpdateDTO.getId()), ErrorCode.PARAMS_ERROR, "id不能为空");

        return ResultUtils.success(wxAccountService.updateAndToRuntime(wxAccountUpdateDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询公众号")
    public BaseResponse<Page<WxAccountVO>> listWxMpAccountByPage(WxAccountPageQueryDTO wxAccountPageQueryDTO){
        long current=wxAccountPageQueryDTO.getCurrent();
        long pageSize=wxAccountPageQueryDTO.getPageSize();

        return ResultUtils.success(wxAccountService.getPage(new Page<>(current,pageSize),
                wxAccountService.getQueryWrapper(wxAccountPageQueryDTO)));
    }

    @GetMapping("/access_token/get")
    @Operation(summary = "测试获取 access_token")
    public BaseResponse<String> getAccessToken(String appId) {
        try {
            wxMpService.switchover(appId);
            return ResultUtils.success(wxMpService.getAccessToken());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }


}
