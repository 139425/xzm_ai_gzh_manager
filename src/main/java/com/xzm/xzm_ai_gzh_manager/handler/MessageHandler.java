package com.xzm.xzm_ai_gzh_manager.handler;






import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpreplyrule.WxReplyContentDTO;
import com.xzm.xzm_ai_gzh_manager.model.enums.WxReplyContentTypeEnum;
import com.xzm.xzm_ai_gzh_manager.service.WxReplyRuleService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.springframework.stereotype.Component;

import java.util.Map;



/**
 * 消息处理器
 */
@Component
@RequiredArgsConstructor
public class MessageHandler implements WxMpMessageHandler {

    private final WxReplyRuleService wxReplyRuleService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) {
        // 获取当前公众号appId
        String appId = WxMpConfigStorageHolder.get();
        // 根据用户消息匹配回复内容
        WxReplyContentDTO replyContent = wxReplyRuleService.receiveMessageReply(appId, wxMpXmlMessage.getContent());

        // 默认回复，当没有匹配到回复规则时使用
        WxMpXmlOutTextMessage defaultReply = WxMpXmlOutMessage.TEXT()
                .content("抱歉，我暂时无法理解您的问题。您可以尝试问其他问题，或者提供更多详细信息。")
                .fromUser(wxMpXmlMessage.getToUser())
                .toUser(wxMpXmlMessage.getFromUser())
                .build();

        // 如果没有匹配到回复内容，返回默认回复
        if (ObjectUtils.isEmpty(replyContent)) {
            return defaultReply;
        }

        // 根据回复内容类型生成对应的回复消息
        WxReplyContentTypeEnum contentTypeEnum = WxReplyContentTypeEnum.getEnumByValue(replyContent.getContentType());
        if (ObjectUtils.isEmpty(contentTypeEnum)) {
            // 内容类型无效，降级为默认回复
            return defaultReply;
        }

        // 根据内容类型生成回复消息
        return wxReplyRuleService.replyByContentType(wxMpXmlMessage, replyContent, contentTypeEnum);
    }
}
