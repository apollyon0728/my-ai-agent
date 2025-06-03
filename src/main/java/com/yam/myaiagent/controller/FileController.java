package com.yam.myaiagent.controller;

import com.yam.myaiagent.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 获取所有Markdown文件列表
     */
    @GetMapping("/markdown")
    public ResponseEntity<List<String>> listMarkdownFiles() {
        List<String> fileList = fileService.listMarkdownFiles();
        return ResponseEntity.ok(fileList);
    }

    /**
     * 以流的方式下载指定的Markdown文件
     */
    @GetMapping("/markdown/{filename:.+}")
    public ResponseEntity<Resource> downloadMarkdownFile(@PathVariable String filename) {
        Resource resource = fileService.loadFileAsResource(filename);
        
        try {
            String contentType = "text/markdown";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}