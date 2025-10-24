package com.yam.myaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @NotNull
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    @NotNull
    @Override
    public AdvisedResponse aroundCall(@NotNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

        advisedRequest = before(advisedRequest);

        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        observeAfter(advisedResponse);

        return advisedResponse;
    }

    /**
     * 执行环绕流式处理逻辑，对请求进行前置处理，传递给下一个处理器，并对响应进行后置观察处理
     *
     * @param advisedRequest 需要被处理的请求对象
     * @param chain          流式处理链，用于传递请求到下一个处理器
     * @return 经过聚合和后置观察处理后的响应流
     */
    @NotNull
    @Override
    public Flux<AdvisedResponse> aroundStream(@NotNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

        // 对请求进行前置处理
        advisedRequest = before(advisedRequest);

        // 获取下一个处理器的响应流
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        // 聚合响应流并应用后置观察处理
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }


}
