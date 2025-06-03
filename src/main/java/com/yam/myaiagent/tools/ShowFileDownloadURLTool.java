package com.yam.myaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 功能：
 * 日期：2025/5/30 12:00
 */
public class ShowFileDownloadURLTool {

    private static final String FILE_DOWNLOAD_URL_PREFIX = "http://localhost:8123/api/files/markdown/{filename}";

    @Tool(description = "If the process already generated doc ,From previous context, Show the download URL of a file for user to download,  FILE_DOWNLOAD_URL_PREFIX is `http://localhost:8123/api/files/markdown/{filename}` ,just add filename")
    public String showFileDownloadURL(@ToolParam(description = "the download URL of a file") String fileDownloadUrl) {
        return "You can download the file from the following URL: " + fileDownloadUrl;
    }
}