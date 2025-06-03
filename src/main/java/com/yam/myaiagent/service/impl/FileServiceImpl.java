package com.yam.myaiagent.service.impl;

import com.yam.myaiagent.constant.FileConstant;
import com.yam.myaiagent.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private final Path markdownLocation;

    public FileServiceImpl() {
        // 配置markdown文件存储路径
        this.markdownLocation = Paths.get(FileConstant.FILE_SAVE_DIR + "/markdown").toAbsolutePath().normalize();
        
        // 确保目录存在
        try {
            Files.createDirectories(this.markdownLocation);
        } catch (IOException ex) {
            throw new RuntimeException("无法创建文件存储目录", ex);
        }
    }

    @Override
    public List<String> listMarkdownFiles() {
        try {
            return Files.walk(this.markdownLocation, 1)
                    .filter(path -> !path.equals(this.markdownLocation))
                    .filter(path -> path.toString().endsWith(".md"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = getMarkdownFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("文件未找到: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("文件未找到: " + filename, e);
        }
    }

    @Override
    public Path getMarkdownFilePath(String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..")) {
            throw new RuntimeException("文件名无效: " + filename);
        }
        return this.markdownLocation.resolve(filename);
    }
}