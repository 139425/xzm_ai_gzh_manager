package com.xzm.xzm_ai_gzh_manager.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xzm.xzm_ai_gzh_manager.common.ErrorCode;
import com.xzm.xzm_ai_gzh_manager.exception.ThrowUtils;
import com.xzm.xzm_ai_gzh_manager.model.entity.WxAccount;
import com.xzm.xzm_ai_gzh_manager.service.WxAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 微信消息接口
 * 用于处理来自微信服务器的请求
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/wx/msg/{appId}")
@Tag(name = "微信消息 - 供微信调用")
@Slf4j
public class WxMpPortalController {
    private final WxMpService wxService;
    private final WxAccountService wxAccountService;


    @GetMapping(produces = "text/plain;charset=utf-8")
    @Operation(summary = "微信服务器的认证消息", description = "公众号接入开发模式时微信调用此接口")
    public String wxServiceAuth(@PathVariable String appId,
                                @RequestParam(name = "signature", required = false) String signature,
                                @RequestParam(name = "timestamp", required = false) String timestamp,
                                @RequestParam(name = "nonce", required = false) String nonce,
                                @RequestParam(name = "echostr", required = false) String echoStr){
        log.info("接收到服务端的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echoStr);

        // 验证请求参数完整性
        ThrowUtils.throwIf(StringUtils.isAnyBlank(signature, timestamp, nonce, echoStr), ErrorCode.PARAMS_ERROR);

        //切换到对应公众号服务
        this.wxService.switchover(appId);

        if(wxService.checkSignature(timestamp, nonce, signature)){
            //设置公众号的认证状态
            wxAccountService.update(
                    Wrappers.lambdaUpdate(WxAccount.class)
                            .eq(WxAccount::getAppId, appId)
                            .set(WxAccount::getVerified,true)
            );

            return echoStr;
        }
    return "非法请求";
    }
}
