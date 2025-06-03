package com.yam.myaiagent.service;

import org.springframework.core.io.Resource;
import java.nio.file.Path;
import java.util.List;

public interface FileService {
    /**
     * 获取markdown目录下所有文件列表
     */
    List<String> listMarkdownFiles();
    
    /**
     * 获取指定的markdown文件资源
     */
    Resource loadFileAsResource(String filename);
    
    /**
     * 获取markdown文件的路径
     */
    Path getMarkdownFilePath(String filename);
}