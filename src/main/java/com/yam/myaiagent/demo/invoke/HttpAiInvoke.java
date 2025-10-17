package com.yam.myaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * HTTP 方式调用 AI
 */
public class HttpAiInvoke {

    /**
     * 主函数，用于调用阿里云DashScope API进行文本生成
     * 该函数构建请求参数，发送HTTP POST请求到DashScope服务，并输出响应结果
     *
     * @param args 命令行参数数组
     */
    public static void main(String[] args) {
        // API密钥
        String apiKey = TestApiKey.API_KEY;

        // 构建请求URL
        // 阿里云DashScope文本生成API服务URL,该URL用于访问阿里云DashScope平台的文本生成服务接口，
        // 支持调用大语言模型进行文本生成任务。
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 构建请求JSON数据
        JSONObject inputJson = new JSONObject();
        JSONObject messagesJson = new JSONObject();

        // 添加系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");

        // 组装messages数组
        messagesJson.set("messages", JSONUtil.createArray().set(systemMessage).set(userMessage));

        // 构建参数
        JSONObject parametersJson = new JSONObject();
        parametersJson.set("result_format", "message");

        // 构建完整请求体
        JSONObject requestJson = new JSONObject();
        requestJson.set("model", "qwen-plus");
        requestJson.set("input", messagesJson);
        requestJson.set("parameters", parametersJson);

        // 发送HTTP POST请求到DashScope API
        // 发送HTTP POST请求到指定URL，设置认证和内容类型头部，发送JSON格式的请求体，并获取响应结果
        String result = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)  // 设置Bearer Token认证头部
                .header("Content-Type", "application/json")   // 设置内容类型为JSON格式
                .body(requestJson.toString())                 // 设置请求体内容
                .execute()                                    // 执行HTTP请求
                .body();                                      // 获取响应体内容


        // 输出API响应结果
        System.out.println(result);
    }

}