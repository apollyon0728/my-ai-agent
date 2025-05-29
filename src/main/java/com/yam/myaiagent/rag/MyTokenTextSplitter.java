package com.yam.myaiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义基于 Token 的切词器
 */
@Component
class MyTokenTextSplitter {
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    /**
     * 第一个参数 200: chunk_size，每个文本块的目标大小（token数）
     * 第二个参数 100: chunk_overlap，相邻文本块的重叠token数
     * 第三个参数 10: 最小chunk大小（token数）
     * 第四个参数 5000: 最大chunk大小（token数）
     * 第五个参数 true: 是否保持句子完整性
     * @param documents
     * @return
     */
    public List<Document> splitCustomized(List<Document> documents) {
//        TokenTextSplitter splitter = new TokenTextSplitter(100, 50, 10, 5000, true);
        TokenTextSplitter splitter = new TokenTextSplitter(
                30,     // chunk_size: 每个块约 60 tokens（强制切分）
                5,     // chunk_overlap: 重叠 15 tokens（保持上下文）
                1,      // minChunkSize: 最小 5 tokens（允许小片段）
                100,    // maxChunkSize: 最大 200 tokens（强制切分长文本）
                false   // keepSplitter: 不严格保持句子完整（允许从中间切分）
        );
        return splitter.apply(documents);
    }
}
