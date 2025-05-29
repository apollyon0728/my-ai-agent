package com.yam.myaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 网页搜索工具 - 使用AI整合搜索API
 */
public class WebSearchTool {

    // AI整合搜索API接口地址
    private static final String AI_SEARCH_API_URL = "https://api.pearktrue.cn/api/aisearch/";

    @Tool(description = "Search for information from AI integrated search engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        try {
            // 构建请求URL - 使用URLUtil.encode进行编码
            String url = AI_SEARCH_API_URL + "?keyword=" + URLUtil.encode(query);

            // 发送GET请求
            String response = HttpUtil.get(url);

            // 解析JSON响应
            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 检查状态码
            if (jsonObject.getInt("code") != 200) {
                return "Search failed: " + jsonObject.getStr("msg");
            }

            // 获取整合后的文本结果
            JSONObject data = jsonObject.getJSONObject("data");
            String textResult = data.getStr("text");

            // 获取相关问题
            JSONArray relatedQuestions = data.getJSONArray("related_questions");
            String questions = relatedQuestions.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n- "));

            // 获取来源信息
            JSONArray sources = data.getJSONArray("sources");
            String sourceInfo = sources.stream()
                    .map(source -> {
                        JSONObject src = (JSONObject) source;
                        return String.format("[%s](%s)\n%s",
                                src.getStr("title"),
                                src.getStr("link"),
                                src.getStr("snippet"));
                    })
                    .collect(Collectors.joining("\n\n"));

            // 组合所有信息
            return String.format("AI整合结果:\n%s\n\n相关问题:\n- %s\n\n信息来源:\n%s",
                    textResult, questions, sourceInfo);

        } catch (Exception e) {
            return "Error searching with AI: " + e.getMessage();
        }
    }
}