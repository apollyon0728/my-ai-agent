package com.yam.myaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    /**
     * 构造函数，用于创建QueryRewriter实例
     *
     * @param dashscopeChatModel 用于构建聊天客户端的ChatModel对象
     * 从代码中的 dashscopeChatModel 参数名可以看出，这里使用的确实是阿里云的模型服务
     * dashscope 是阿里云旗下的通义千问API服务平台的名称
     * 代码中通过 ChatClient.builder(dashscopeChatModel) 构建了聊天客户端，表明使用的是阿里云的 dashscope 服务
     * 因此，可以确认这里使用的model是阿里的服务
     */
    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        // 创建查询重写转换器
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }


    /**
     * 执行查询重写
     * 这段代码的功能是执行查询重写：
     * 1. 将输入的`prompt`字符串封装成`Query`对象
     * 2. 调用`queryTransformer.transform()`方法对查询进行重写转换
     * 3. 返回重写后的查询文本内容
     * 主要作用是通过预定义的转换器优化或改写用户的查询语句。
     *
     * @param prompt 用户输入的查询
     * @return 重写后的查询文本内容
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 输出重写后的查询
        return transformedQuery.text();
    }
}
