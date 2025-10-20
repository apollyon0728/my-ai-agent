package com.yam.myaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.yam.myaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类（提供文件读写功能）
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    /**
     * 读取文件内容
     *
     * @param fileName 要读取的文件名
     * @return 文件内容字符串，如果读取失败则返回错误信息
     */
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        // 构造文件完整路径
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 读取文件UTF-8编码内容
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            // 返回错误信息
            return "Error reading file: " + e.getMessage();
        }
    }


    /**
     * 将内容写入指定文件
     *
     * @param fileName 要写入的文件名
     * @param content  要写入文件的内容
     * @return 操作结果信息，成功时返回文件路径，失败时返回错误信息
     */
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content
    ) {
        String filePath = FILE_DIR + "/" + fileName;

        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }

}
