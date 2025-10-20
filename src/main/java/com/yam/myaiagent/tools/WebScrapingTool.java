package com.yam.myaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {

    /**
     * 抓取网页内容工具方法
     * 该方法使用 Jsoup 库从给定的 URL 抓取网页内容，并返回抓取结果。
     *
     * @param url 要抓取的网页 URL
     * @return 抓取到的网页内容，如果发生错误则返回错误信息
     */
    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
