package com.xzm.xzm_ai_gzh_manager.handler;


import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xzm.xzm_ai_gzh_manager.constant.RedisConstant;
import com.xzm.xzm_ai_gzh_manager.manager.DistributedLockManager;
import com.xzm.xzm_ai_gzh_manager.model.dto.wxmpreplyrule.WxReplyContentDTO;
import com.xzm.xzm_ai_gzh_manager.model.entity.AiReplyRecord;
import com.xzm.xzm_ai_gzh_manager.model.enums.WxAiReplyStatusEnum;
import com.xzm.xzm_ai_gzh_manager.model.enums.WxReplyContentTypeEnum;
import com.xzm.xzm_ai_gzh_manager.service.AiReplyRecordService;
import com.xzm.xzm_ai_gzh_manager.service.WxReplyRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MessageHandler implements WxMpMessageHandler {

    private final WxReplyRuleService wxReplyRuleService;
    private final AiReplyRecordService aiReplyRecordService;
    private final DistributedLockManager distributedLockManager;

    /**
     * 处理微信消息的核心方法
     */
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) {
        String appId = WxMpConfigStorageHolder.get();
        String userMessage = wxMpXmlMessage.getContent();
        String fromUser = wxMpXmlMessage.getFromUser();

        // 创建默认回复消息
        WxMpXmlOutTextMessage defaultReplyMessage = WxMpXmlOutMessage.TEXT()
                .content(String.format("正在思考中，请 10 秒后再次发送原问题：%s", userMessage))
                .fromUser(wxMpXmlMessage.getToUser())
                .toUser(fromUser)
                .build();

        // 构建分布式锁的key，针对公众号和用户加锁，避免短时间内重复处理相同消息
        String lock = RedisConstant.MESSAGE_REPLY_LOCK + appId + ":" + fromUser + ":" + DigestUtil.md5Hex(userMessage);

        // 使用分布式锁管理器执行操作
        return distributedLockManager.nonBlockExecute(lock, () -> {
            // 获取锁成功，处理消息（以下是消息处理的业务逻辑）
            WxReplyContentDTO replyContent = wxReplyRuleService.receiveMessageReply(appId, userMessage);
            if (ObjectUtils.isEmpty(replyContent)) {
                AiReplyRecord replyRecord = aiReplyRecordService.lambdaQuery()
                        .eq(AiReplyRecord::getFromUser, fromUser)
                        .eq(AiReplyRecord::getAppId, appId)
                        .eq(AiReplyRecord::getMessage, userMessage)
                        .eq(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.NOT_REPLY.getValue())
                        .one();

                if (ObjectUtils.isEmpty(replyRecord)) {
                    AiReplyRecord aiReplyRecord = new AiReplyRecord();
                    aiReplyRecord.setAppId(appId);
                    aiReplyRecord.setFromUser(fromUser);
                    aiReplyRecord.setMessage(userMessage);
                    aiReplyRecordService.save(aiReplyRecord);
                    String content = aiReplyRecordService.aiReply(appId, fromUser, userMessage, aiReplyRecord);
                    if (StringUtils.isBlank(content)) {
                        // 如果回复消息为空，代表此时没有 AI 回复
                        return defaultReplyMessage;
                    }
                    return WxMpXmlOutMessage.TEXT().content(content)
                            .fromUser(wxMpXmlMessage.getToUser())
                            .toUser(fromUser)
                            .build();
                }
                if (ObjectUtils.isEmpty(replyRecord.getReplyMessage())) {
                    // 如果回复消息为空，代表此时没有 AI 回复
                    return defaultReplyMessage;
                }
                aiReplyRecordService.lambdaUpdate()
                        .set(AiReplyRecord::getReplyStatus, WxAiReplyStatusEnum.REPLIED.getValue())
                        .eq(AiReplyRecord::getId, replyRecord.getId())
                        .update();
                return WxMpXmlOutMessage.TEXT().content(replyRecord.getReplyMessage())
                        .fromUser(wxMpXmlMessage.getToUser())
                        .toUser(fromUser)
                        .build();
            }

            WxReplyContentTypeEnum contentTypeEnum = WxReplyContentTypeEnum.getEnumByValue(replyContent.getContentType());
            if (ObjectUtils.isEmpty(contentTypeEnum)) {
                // 降级回复
                return WxMpXmlOutMessage.TEXT().content("抱歉，我暂时无法理解您的问题。您可以尝试问其他问题，或者提供更多详细信息。")
                        .fromUser(wxMpXmlMessage.getToUser())
                        .toUser(fromUser)
                        .build();
            }
            return wxReplyRuleService.replyByContentType(wxMpXmlMessage, replyContent, contentTypeEnum);
        }, () -> defaultReplyMessage); // 获取锁失败，返回默认回复
    }
}
