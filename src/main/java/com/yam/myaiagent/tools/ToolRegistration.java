package com.yam.myaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {


    /**
     * 获取所有可用的工具回调数组
     *
     * @return ToolCallback[] 包含所有已初始化工具的回调数组
     */
    @Bean
    public ToolCallback[] allTools() {
        // 初始化各种工具实例
        // 创建文件操作工具实例，用于执行文件相关的操作
        FileOperationTool fileOperationTool = new FileOperationTool();

        // 创建网络搜索工具实例，用于执行网络搜索功能
        WebSearchTool webSearchTool = new WebSearchTool();

        // 创建网页抓取工具实例，用于从网页中提取数据
        WebScrapingTool webScrapingTool = new WebScrapingTool();

        // 创建资源下载工具实例，用于下载网络资源
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();

        // 创建终端操作工具实例，用于执行系统终端命令
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();

        // 创建PDF生成工具实例，用于生成PDF文档
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();

        // 创建Markdown生成工具实例，用于生成Markdown格式文档
        MarkdownGenerationTool markdownGenerationTool = new MarkdownGenerationTool();

        // 创建天气搜索工具实例，用于查询天气信息
        WeatherSearchTool weatherSearchTool = new WeatherSearchTool();

        // 创建终止工具实例，用于结束程序执行
        TerminateTool terminateTool = new TerminateTool();

//        ShowFileDownloadURLTool showFileDownloadURLTool = new ShowFileDownloadURLTool();

        // 将所有工具转换为回调数组并返回
        return ToolCallbacks.from(
//                showFileDownloadURLTool,
                weatherSearchTool,
                markdownGenerationTool,
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
        );
    }

}
