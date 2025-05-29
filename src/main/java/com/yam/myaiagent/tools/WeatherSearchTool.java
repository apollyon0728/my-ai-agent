package com.yam.myaiagent.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

/**
 * 天气查询工具类
 */
public class WeatherSearchTool {

    @Tool(description = "Search for weather information by city name")
    public String searchWeather(
            @ToolParam(description = "the name of search city") String city) {

        // 构建请求URL
        String url = String.format("https://api.pearktrue.cn/api/weather/?city=%s&id=1", city);

        try {
            // 发送GET请求
            HttpResponse response = HttpRequest.get(url).execute();

            if (response.isOk()) {
                String body = response.body();
                JSONObject json = JSONUtil.parseObj(body);

                // 解析响应数据
                if (json.getInt("code") == 200) {
                    String cityName = json.getStr("city");
                    List<Map<String, Object>> data = json.get("data", List.class);

                    StringBuilder result = new StringBuilder();
                    result.append("城市: ").append(cityName).append("\n");
                    result.append("天气预报:\n");

                    for (Map<String, Object> day : data) {
                        result.append(day.get("date")).append(": ")
                                .append(day.get("weather")).append(", ")
                                .append("温度: ").append(day.get("temperature")).append(", ")
                                .append(day.get("wind")).append(" ").append(day.get("wind_level")).append(", ")
                                .append("空气质量: ").append(day.get("air_quality")).append("\n");
                    }

                    return result.toString();
                } else {
                    return "查询失败: " + json.getStr("msg");
                }
            } else {
                return "请求天气API失败，状态码: " + response.getStatus();
            }
        } catch (Exception e) {
            return "查询天气时发生错误: " + e.getMessage();
        }
    }
}