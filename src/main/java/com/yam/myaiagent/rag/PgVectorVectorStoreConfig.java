package com.yam.myaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // 可选：默认为模型维度或1536
                .distanceType(COSINE_DISTANCE)       // 可选：默认为余弦距离
                .indexType(HNSW)                     // 可选：默认为HNSW
                .initializeSchema(true)              // 可选：默认为false
                .schemaName("public")                // 可选：默认为“公开”
                .vectorTableName("vector_store")     // 可选：默认为“vector_store”
                .maxDocumentBatchSize(10000)         // 可选：默认为10000
                .build();
        // 加载文档
//        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//        vectorStore.add(documents);
        return vectorStore;
    }
}
