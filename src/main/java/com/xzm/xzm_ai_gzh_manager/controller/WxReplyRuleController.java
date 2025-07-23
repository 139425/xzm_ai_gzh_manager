package com.xzm.xzm_ai_gzh_manager.controller;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzm.xzm_ai_gzh_manager.common.BaseResponse;
import com.xzm.xzm_ai_gzh_manager.common.ErrorCode;
import com.xzm.xzm_ai_gzh_manager.common.ResultUtils;
import com.xzm.xzm_ai_gzh_manager.exception.BusinessException;
import com.xzm.xzm_ai_gzh_manager.exception.ThrowUtils;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpreplyrule.WxReplyRuleAddRequest;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpreplyrule.WxReplyRulePageQueryRequest;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpreplyrule.WxReplyRuleUpdateRequest;
import com.xzm.xzm_ai_gzh_manager.model.entity.WxReplyRule;
import com.xzm.xzm_ai_gzh_manager.model.enums.WxReplyRuleTypeEnum;
import com.xzm.xzm_ai_gzh_manager.model.vo.WxReplyRuleVO;
import com.xzm.xzm_ai_gzh_manager.service.UserService;
import com.xzm.xzm_ai_gzh_manager.service.WxReplyRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/wx/reply")
@Tag(name = "自动回复规则管理")
@RequiredArgsConstructor
@Slf4j
public class WxReplyRuleController {

    private final WxReplyRuleService wxReplyRuleService;

    private final UserService userService;


    /**
     * 新增
     */
    @PostMapping("/add")
    @Operation(summary = "新增回复规则")
    public BaseResponse<Long> addWxReplyRule(@Valid @RequestBody WxReplyRuleAddRequest wxReplyRuleAddRequest, HttpServletRequest request) {
        if (ObjectUtils.isEmpty(wxReplyRuleAddRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer replyType = wxReplyRuleAddRequest.getReplyType();

        // 当为菜单类型时
        if (Objects.equals(replyType, WxReplyRuleTypeEnum.EVENT.getValue())) {

            ThrowUtils.throwIf(StringUtils.isBlank(wxReplyRuleAddRequest.getEventKey()),
                    ErrorCode.PARAMS_ERROR, "菜单栏点击事件key不能为空");

            ThrowUtils.throwIf(wxReplyRuleService.lambdaQuery()
                            .eq(WxReplyRule::getEventKey, wxReplyRuleAddRequest.getEventKey())
                            .eq(WxReplyRule::getAppId, wxReplyRuleAddRequest.getAppId())
                            .exists(),
                    ErrorCode.PARAMS_ERROR, "当前key值已被使用");
        }

        ThrowUtils.throwIf(Objects.equals(replyType, WxReplyRuleTypeEnum.KEYWORDS.getValue()) &&
                        ObjectUtils.isEmpty(wxReplyRuleAddRequest.getMatchValue()),
                ErrorCode.PARAMS_ERROR, "关键字不能为空");

        WxReplyRule wxReplyRule = wxReplyRuleAddRequest.toWxReplyRule();
        wxReplyRule.setUserId(userService.getLoginUser(request).getId());
        wxReplyRuleService.save(wxReplyRule);
        return ResultUtils.success(wxReplyRule.getId());
    }

    /**
     * 更新回复规则
     *
     * @param wxReplyRuleUpdateRequest 更新请求对象
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新回复规则")
    public BaseResponse<Boolean> updateWxReplyRule(@RequestBody WxReplyRuleUpdateRequest wxReplyRuleUpdateRequest){

        if(ObjectUtils.isEmpty(wxReplyRuleUpdateRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断是否要修改菜单栏点击事件的key
        WxReplyRule wxReplyRuleDb = wxReplyRuleService.getById(wxReplyRuleUpdateRequest.getId());
        if(ObjectUtil.notEqual(wxReplyRuleDb.getEventKey(), wxReplyRuleUpdateRequest.getEventKey())){
            //判断是否已经被使用
            String appid= wxReplyRuleDb.getAppId();
            ThrowUtils.throwIf(wxReplyRuleService.count(
                    Wrappers.lambdaQuery(WxReplyRule.class)
                            .eq(WxReplyRule::getEventKey, wxReplyRuleUpdateRequest.getEventKey())
                            .eq(WxReplyRule::getAppId, StringUtils.isBlank(appid)? wxReplyRuleDb.getAppId() : appid)
            )>0, ErrorCode.PARAMS_ERROR, String.format("当前key值已被使用，请更换其他key值，当前key值：%s", wxReplyRuleDb.getEventKey()));
        }
        return ResultUtils.success(wxReplyRuleService.updateById(wxReplyRuleUpdateRequest.toWxReplyRule()));
    }

    /**
     * 分页查询回复规则
     *
     * @param wxReplyRulePageQueryRequest 分页查询请求
     * @return 回复规则分页数据
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询回复规则")
    public BaseResponse<Page<WxReplyRuleVO>> listWxMpReplyRuleByPage(WxReplyRulePageQueryRequest wxReplyRulePageQueryRequest){
        long current = wxReplyRulePageQueryRequest.getCurrent();
        long size = wxReplyRulePageQueryRequest.getPageSize();
        return ResultUtils.success(wxReplyRuleService.getPage(new Page<>(current, size),
                wxReplyRuleService.getQueryWrapper(wxReplyRulePageQueryRequest)));
    }


    /**
     * 根据ID查询回复规则详情
     *
     * @param id 规则ID
     * @return 回复规则详情
     */
    @GetMapping("/get/vo")
    @Operation(summary = "回复规则详情")
    public BaseResponse<WxReplyRuleVO> getWxMpReplyRuleVOById(Long id) {
        // 查询规则
        WxReplyRule wxReplyRule = wxReplyRuleService.getById(id);

        // 验证规则是否存在
        ThrowUtils.throwIf(ObjectUtils.isEmpty(wxReplyRule), ErrorCode.NOT_FOUND_ERROR);

        // 转换为 VO
        WxReplyRuleVO replyRuleVO = WxReplyRuleVO.obj2VO(wxReplyRule);

        // 添加创建者信息
        replyRuleVO.setUser(userService.getUserVO(userService.getById(wxReplyRule.getUserId())));
        return ResultUtils.success(replyRuleVO);
    }

}
