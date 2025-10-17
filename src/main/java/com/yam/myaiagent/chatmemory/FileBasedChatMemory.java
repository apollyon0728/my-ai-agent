package com.yam.myaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件持久化的对话记忆
 */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 向指定对话添加消息列表
     *
     * @param conversationId 对话ID，用于标识要添加消息的对话
     * @param messages 要添加的消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取或创建指定ID的对话消息列表
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        // 将新消息添加到对话中
        conversationMessages.addAll(messages);
        // 保存更新后的对话消息列表
        saveConversation(conversationId, conversationMessages);
    }


     /**
     * 获取指定对话的最后N条消息
     *
     * @param conversationId 对话ID
     * @param lastN 要获取的消息数量
     * @return 包含最后N条消息的列表
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 获取或创建对话消息列表
        List<Message> allMessages = getOrCreateConversation(conversationId);

        // 跳过前面的消息，只保留最后N条消息
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }


    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

     /**
     * 获取或创建对话消息列表
     *
     * @param conversationId 对话ID，用于标识唯一的对话
     * @return 返回对应对话ID的消息列表，如果文件不存在则返回空列表
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        // 如果对话文件存在，则从文件中读取消息列表
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }


    /**
     * 保存对话消息到文件
     *
     * @param conversationId 对话ID，用于标识唯一的对话
     * @param messages 消息列表，包含该对话的所有消息内容
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        // 获取对话文件对象
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            // 使用Kryo序列化消息列表并写入文件
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
