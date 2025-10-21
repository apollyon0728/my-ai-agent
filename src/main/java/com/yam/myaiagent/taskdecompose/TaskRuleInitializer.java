package com.yam.myaiagent.taskdecompose;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 任务拆解规则初始化器
 * 在应用启动时自动加载规则文件到向量数据库
 */
@Component
@Slf4j
public class TaskRuleInitializer {

    @Resource
    private TaskDecomposer taskDecomposer;

    /**
     * 初始化方法，在应用启动时自动运行
     * 加载规则文件到向量数据库
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化任务拆解规则...");

        try {
            // 加载规则目录下的所有JSON文件
            ClassPathResource resource = new ClassPathResource("task-rules");
            Path rulesDir = Paths.get(resource.getURI());

            // FIXME MY：注释掉，要不然每次都在向量数据库中增加规则

            // 加载规则目录中的所有JSON规则文件, 检查规则目录是否存在且为目录类型
//            if (Files.exists(rulesDir) && Files.isDirectory(rulesDir)) {
//                // 遍历目录下的所有文件，筛选出JSON文件并加载
//                try (Stream<Path> paths = Files.walk(rulesDir)) {
//                    paths.filter(path -> path.toString().endsWith(".json"))
//                            .forEach(this::loadRuleFile);
//                }
//            } else {
//                // 记录警告日志：规则目录不存在
//                log.warn("任务拆解规则目录不存在: {}", rulesDir);
//            }

        } catch (IOException e) {
            log.error("加载任务拆解规则失败", e);
        }

        log.info("任务拆解规则初始化完成");
    }

    /**
     * 加载单个规则文件
     *
     * @param filePath 规则文件路径
     */
    private void loadRuleFile(Path filePath) {
        try {
            log.info("加载规则文件: {}", filePath.getFileName());

            // 读取文件内容
            String ruleJson = Files.readString(filePath, StandardCharsets.UTF_8);

            // 上传规则到向量数据库
            String ruleId = taskDecomposer.uploadRule(ruleJson);

            log.info("规则上传成功: {}, 规则ID: {}", filePath.getFileName(), ruleId);
        } catch (Exception e) {
            log.error("加载规则文件失败: {}", filePath.getFileName(), e);
        }
    }
}