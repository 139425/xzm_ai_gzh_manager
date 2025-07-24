package com.xzm.xzm_ai_gzh_manager.aitest;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeepSeekTest {

    @Resource
    //private OpenAiChatModel chatModel;
    private ZhiPuAiChatModel chatModel;
    @Test
    public void testDeepSeek() {
        // 构建提示词，包含系统角色定义和用户问题
        ChatResponse chatResponse = chatModel.call(
                new Prompt(
                     //   new SystemMessage("我想让你充当一个拥有十年开发经验的架构师，对多种编程语言和技术栈有深入的了解，精通编程原理、算法、数据结构以及调试技巧，能够有效地沟通和解释复杂概念，提供清晰、准确、有用的回答，帮助提问者解决问题，并提升个人在专业领域的声誉。回答需要保持专业、尊重和客观，避免使用过于复杂或初学者难以理解的术语。我会问与编程相关的问题，你会回答应该是什么答案，并在不够详细的时候写解释，并且回答的内容尽量使用HTML格式。"),
                        new UserMessage("请你介绍一下什么是 Java")
                )
        );
        System.out.println(chatResponse);
    }

    @Test
    public void zhipuTest() {
        // 构建提示词，包含系统角色定义和用户问题
//        ChatResponse response = chatModel.call(
//                new Prompt(
//                        "Generate the names of 5 famous pirates.",
//                        ZhiPuAiChatOptions.builder()
//                                .model(ZhiPuAiApi.ChatModel.GLM_3_Turbo.getValue())
//                                .temperature(0.5)
//                                .build()
//                ));

        ChatResponse chatResponse = chatModel.call(
                new Prompt(
                        new SystemMessage("我想让你充当一个拥有十年开发经验的架构师，对多种编程语言和技术栈有深入的了解，精通编程原理、算法、数据结构以及调试技巧，能够有效地沟通和解释复杂概念，提供清晰、准确、有用的回答，帮助提问者解决问题，并提升个人在专业领域的声誉。回答需要保持专业、尊重和客观，避免使用过于复杂或初学者难以理解的术语。我会问与编程相关的问题，你会回答应该是什么答案，回复内容控制在 200 字以内，并且回答的内容不要使用 markdown 格式，如果有链接可以使用 HTML 格式展示。"),
                        new UserMessage("请你介绍一下什么是 Java")
                )
        );
        System.out.println(chatResponse);
    }
}
