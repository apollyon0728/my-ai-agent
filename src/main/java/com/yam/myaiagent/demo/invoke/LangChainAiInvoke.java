package com.yam.myaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChainAiInvoke {

    public static void main(String[] args) {
        // 构建Qwen聊天模型实例
        // 通过QwenChatModel的builder模式创建聊天模型对象，配置API密钥和模型名称
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)  // 设置API访问密钥
                .modelName("qwen-max")        // 指定使用的模型名称为qwen-max
                .build();                     // 构建并返回ChatLanguageModel实例

        String answer = qwenChatModel.chat("我是，这是  的 AI 超级智能体原创项目");
        System.out.println(answer);
    }
}
