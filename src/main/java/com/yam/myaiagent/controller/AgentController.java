package com.yam.myaiagent.controller;

import com.yam.myaiagent.agent.MyManus;
import com.yam.myaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 功能：
 * 日期：2025/5/29 15:37
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        MyManus yuManus = new MyManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }
}