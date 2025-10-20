package com.yam.myaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 工具类用于显示文件下载URL
 */
public class ShowFileDownloadURLTool {

    /**
     * 文件下载URL前缀常量
     */
    private static final String FILE_DOWNLOAD_URL_PREFIX = "http://localhost:8123/api/files/markdown/{filename}";

    /**
     * 显示文件下载URL工具方法
     * 该方法用于生成并返回文件的下载链接，供用户下载已生成的文档
     *
     * @param fileDownloadUrl 文件下载URL地址
     * @return 包含下载链接的提示信息字符串
     */
    @Tool(description = "If the process already generated doc ,From previous context, Show the download URL of a file for user to download,  FILE_DOWNLOAD_URL_PREFIX is `http://localhost:8123/api/files/markdown/{filename}` ,just add filename")
    public String showFileDownloadURL(@ToolParam(description = "the download URL of a file") String fileDownloadUrl) {
        return "You can download the file from the following URL: " + fileDownloadUrl;
    }
}
