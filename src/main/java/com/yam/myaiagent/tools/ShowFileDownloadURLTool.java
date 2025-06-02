package com.yam.myaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 功能：
 * 日期：2025/5/30 12:00
 */
public class ShowFileDownloadURLTool {

    @Tool(description = "If the process already generated doc , From previous context, Show the download URL of a file for user to download")
    public String showFileDownloadURL(@ToolParam(description = "the download URL of a file") String fileDownloadUrl) {
        return "You can download the file from the following URL: " + fileDownloadUrl;
    }
}