package com.yam.myaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {


    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        MarkdownGenerationTool markdownGenerationTool = new MarkdownGenerationTool();
        WeatherSearchTool weatherSearchTool = new WeatherSearchTool();
        TerminateTool terminateTool = new TerminateTool();
//        ShowFileDownloadURLTool showFileDownloadURLTool = new ShowFileDownloadURLTool();
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
