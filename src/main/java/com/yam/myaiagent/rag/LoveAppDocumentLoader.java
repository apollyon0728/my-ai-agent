package com.yam.myaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱大师应用文档加载器
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇 Markdown 文档
     * @return
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                // 提取文档倒数第 3 和第 2 个字作为标签
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(markdownDocumentReader.get());
            }
        } catch (IOException e) {
           log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }

    public List<Document> loadMarkdowns(File file) {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                // 提取文档倒数第 3 和第 2 个字作为标签
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(markdownDocumentReader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }


        /**
     * 根据文件路径加载Markdown文档
     *
     * @param filePath Markdown文件的完整路径
     * @return 包含解析后文档的列表，如果加载失败或文件不存在则返回空列表
     */
    public List<Document> loadMarkdownByPath(String filePath) {
        List<Document> allDocuments = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在或不是文件: {}", filePath);
                return allDocuments;
            }
            String filename = file.getName();
            // 从文件名中提取状态信息（文件名倒数第4-6个字符）
            String status = filename.length() >= 6 ? filename.substring(filename.length() - 6, filename.length() - 4) : "";
            // 配置Markdown文档读取器，设置解析选项和元数据
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(false)
                    .withIncludeBlockquote(false)
                    .withAdditionalMetadata("filename", filename)
                    .withAdditionalMetadata("status", status)
                    .build();
            // 用 FileSystemResource 包装 file
            FileSystemResource resource = new FileSystemResource(file);
            MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
            allDocuments.addAll(markdownDocumentReader.get());
        } catch (Exception e) {
            log.error("Markdown 文档加载失败: {}", filePath, e);
        }
        return allDocuments;
    }

}
