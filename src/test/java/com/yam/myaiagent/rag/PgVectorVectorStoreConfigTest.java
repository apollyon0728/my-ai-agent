package com.yam.myaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStoreChineseTest() {
        List<Document> documents = List.of(
                new Document("ai 是 未来的趋势啊"),
                new Document("春天来了，万物复苏。", Map.of("category", "自然")),
                new Document("人工智能正在改变世界。", Map.of("category", "科技")),
                new Document("人工智能。"),
                new Document("生活就像一盒巧克力，你永远不知道会得到什么。", Map.of("category", "哲理"))
        );

        // 添加中文文档到PGVector
        pgVectorVectorStore.add(documents);

        // 用中文查询相似内容
        List<Document> results = this.pgVectorVectorStore.similaritySearch(
                SearchRequest.builder().query("人工智能").topK(1).build()
        );
        for (int i = 0; i < results.size(); i++) {
            System.out.println(results.get(i));
        }
        System.out.println("---------------------------------------------------------------------");

        // 简单断言，确保有返回结果
        Assertions.assertFalse(results.isEmpty(), "未检索到相似中文文档！");
    }
}
