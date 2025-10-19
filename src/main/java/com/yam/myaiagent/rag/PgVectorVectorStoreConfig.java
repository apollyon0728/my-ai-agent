package com.yam.myaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 *  PGVector 向量存储配置
 */

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * 创建并配置PgVector向量存储Bean
     *
     * @param jdbcTemplate 数据库操作模板，用于执行SQL语句
     * @param dashscopeEmbeddingModel 嵌入模型，用于将文本转换为向量表示
     * @return 配置好的向量存储实例
     */
    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 构建PgVector存储配置
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // 可选：默认为模型维度或1536
                .distanceType(COSINE_DISTANCE)       // 可选：默认为余弦距离
                .indexType(HNSW)                     // 可选：默认为HNSW
                .initializeSchema(false)             // 修改为false，避免每次启动都重新初始化表结构
                .schemaName("public")                // 可选：默认为"public"
                .vectorTableName("vector_store")     // 可选：默认为"vector_store"
                .maxDocumentBatchSize(10000)         // 可选：默认为10000
                .build();
        // 每次启动都会加载文档，加载一次即可
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        vectorStore.add(documents);
        return vectorStore;
    }

}
