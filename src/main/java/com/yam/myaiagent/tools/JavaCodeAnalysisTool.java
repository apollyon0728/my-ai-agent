package com.yam.myaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yam.myaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Java代码分析工具
 * 用于分析Java代码质量、性能和最佳实践
 *
 * @author cocoyu
 * @date 2024-09-24
 */
public class JavaCodeAnalysisTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/java-analysis";

    /**
     * 分析Java代码质量，检查常见问题和性能优化建议
     *
     * @param javaCode 要分析的Java代码
     * @return 代码分析报告字符串，包含语法、性能、最佳实践、安全性和代码风格等方面的检查结果
     */
    @Tool(description = "分析Java代码质量，检查常见问题和性能优化建议")
    public String analyzeJavaCode(@ToolParam(description = "要分析的Java代码") String javaCode) {
        try {
            if (StrUtil.isBlank(javaCode)) {
                return "请提供需要分析的Java代码";
            }

            StringBuilder analysis = new StringBuilder();
            analysis.append("=== Java代码分析报告 ===\n\n");

            // 基础语法检查
            analysis.append("【基础语法检查】\n");
            checkBasicSyntax(javaCode, analysis);
            analysis.append("\n");

            // 性能分析
            analysis.append("【性能分析】\n");
            checkPerformance(javaCode, analysis);
            analysis.append("\n");

            // 最佳实践检查
            analysis.append("【最佳实践检查】\n");
            checkBestPractices(javaCode, analysis);
            analysis.append("\n");

            // 安全性检查
            analysis.append("【安全性检查】\n");
            checkSecurity(javaCode, analysis);
            analysis.append("\n");

            // 代码风格检查
            analysis.append("【代码风格检查】\n");
            checkCodeStyle(javaCode, analysis);
            analysis.append("\n");

            // 保存分析结果
            String fileName = "java_code_analysis_" + System.currentTimeMillis() + ".txt";
            saveAnalysisResult(fileName, analysis.toString());

            analysis.append("分析完成！报告已保存到文件：").append(fileName);
            return analysis.toString();

        } catch (Exception e) {
            return "代码分析失败: " + e.getMessage();
        }
    }


    /**
     * 生成Java面试题目和标准答案
     *
     * @param level  面试级别：junior/middle/senior
     * @param domain 技术领域：basic/spring/jvm/database/algorithm等
     * @return 包含面试题目的字符串，如果生成失败则返回错误信息
     */
    @Tool(description = "生成Java面试题目和标准答案")
    public String generateJavaInterviewQuestions(@ToolParam(description = "面试级别：junior/middle/senior") String level,
                                                 @ToolParam(description = "技术领域：basic/spring/jvm/database/algorithm等") String domain) {
        try {
            StringBuilder questions = new StringBuilder();
            questions.append("=== Java面试题库 ===\n");
            questions.append("级别：").append(level).append("\n");
            questions.append("领域：").append(domain).append("\n\n");

            // 根据级别和领域生成题目
            generateQuestionsByLevelAndDomain(level, domain, questions);

            // 保存题目到文件
            String fileName = String.format("java_interview_%s_%s_%d.md", level, domain, System.currentTimeMillis());
            saveAnalysisResult(fileName, questions.toString());

            questions.append("\n\n题目已保存到文件：").append(fileName);
            return questions.toString();

        } catch (Exception e) {
            return "生成面试题目失败: " + e.getMessage();
        }
    }


    /**
     * 对Java代码进行性能优化分析并提供优化建议
     *
     * @param javaCode 需要优化的Java代码字符串
     * @return 包含优化建议的字符串，如果分析失败则返回错误信息
     */
    @Tool(description = "Java代码性能优化建议")
    public String optimizeJavaCode(@ToolParam(description = "需要优化的Java代码") String javaCode) {
        try {
            StringBuilder optimization = new StringBuilder();
            optimization.append("=== Java代码优化建议 ===\n\n");

            // FIXME 分析代码并给出优化建议
            analyzeForOptimization(javaCode, optimization);

            return optimization.toString();
        } catch (Exception e) {
            return "代码优化分析失败: " + e.getMessage();
        }
    }


    /**
     * 基础语法检查
     */
    private void checkBasicSyntax(String code, StringBuilder analysis) {
        if (code.contains("System.out.print")) {
            analysis.append("⚠️ 发现System.out.print语句，建议使用日志框架（如SLF4J）\n");
        }
        if (code.contains("printStackTrace()")) {
            analysis.append("⚠️ 发现printStackTrace()，建议使用日志记录异常\n");
        }
        if (!code.contains("private") && !code.contains("public") && !code.contains("protected")) {
            analysis.append("⚠️ 建议明确指定访问修饰符\n");
        }
        if (code.contains("String +") && code.split("String \\+").length > 3) {
            analysis.append("⚠️ 发现多次字符串连接，建议使用StringBuilder\n");
        }
        analysis.append("✅ 基础语法检查完成\n");
    }

    /**
     * 性能分析
     */
    private void checkPerformance(String code, StringBuilder analysis) {
        if (code.contains("new ArrayList<>()") && code.contains("for")) {
            analysis.append("💡 建议预估ArrayList初始容量以避免扩容\n");
        }
        if (code.contains("synchronized") && code.contains("method")) {
            analysis.append("💡 考虑使用更细粒度的锁或并发集合\n");
        }
        if (code.contains("HashMap") && code.contains("thread")) {
            analysis.append("⚠️ HashMap非线程安全，多线程环境建议使用ConcurrentHashMap\n");
        }
        analysis.append("✅ 性能分析完成\n");
    }

    /**
     * 最佳实践检查
     */
    private void checkBestPractices(String code, StringBuilder analysis) {
        if (!code.contains("@Override") && code.contains("public") && code.contains("toString")) {
            analysis.append("💡 重写方法建议添加@Override注解\n");
        }
        if (code.contains("catch") && !code.contains("log")) {
            analysis.append("💡 异常处理建议添加日志记录\n");
        }
        if (code.contains("static final") && !code.matches(".*[A-Z_]+.*")) {
            analysis.append("💡 常量建议使用大写字母和下划线命名\n");
        }
        analysis.append("✅ 最佳实践检查完成\n");
    }

    /**
     * 安全性检查
     */
    private void checkSecurity(String code, StringBuilder analysis) {
        if (code.contains("SQL") || code.contains("sql")) {
            analysis.append("⚠️ 检测到SQL相关代码，注意防止SQL注入\n");
        }
        if (code.contains("password") || code.contains("secret")) {
            analysis.append("⚠️ 检测到敏感信息，注意加密存储\n");
        }
        if (code.contains("Runtime.exec") || code.contains("ProcessBuilder")) {
            analysis.append("⚠️ 检测到系统命令执行，注意输入验证\n");
        }
        analysis.append("✅ 安全性检查完成\n");
    }

    /**
     * 代码风格检查
     */
    private void checkCodeStyle(String code, StringBuilder analysis) {
        if (!code.matches(".*\\{\\s*\\n.*")) {
            analysis.append("💡 建议使用标准的代码格式化风格\n");
        }
        if (code.split("\\n").length > 50) {
            analysis.append("💡 方法过长，建议拆分为多个小方法\n");
        }
        if (!code.contains("/**") && code.contains("public class")) {
            analysis.append("💡 建议添加JavaDoc注释\n");
        }
        analysis.append("✅ 代码风格检查完成\n");
    }

    /**
     * 根据级别和领域生成题目
     */
    private void generateQuestionsByLevelAndDomain(String level, String domain, StringBuilder questions) {
        if ("junior".equalsIgnoreCase(level)) {
            generateJuniorQuestions(domain, questions);
        } else if ("middle".equalsIgnoreCase(level)) {
            generateMiddleQuestions(domain, questions);
        } else if ("senior".equalsIgnoreCase(level)) {
            generateSeniorQuestions(domain, questions);
        }
    }

    private void generateJuniorQuestions(String domain, StringBuilder questions) {
        questions.append("## 初级Java面试题\n\n");
        if ("basic".equalsIgnoreCase(domain)) {
            questions.append("### 1. Java基础概念\n");
            questions.append("**问题**: 什么是面向对象编程？Java中的三大特性是什么？\n");
            questions.append("**答案**: 面向对象编程是一种编程范式，Java的三大特性是封装、继承、多态...\n\n");

            questions.append("**问题**: String、StringBuilder、StringBuffer的区别？\n");
            questions.append("**答案**: String是不可变的，StringBuilder是可变的非线程安全，StringBuffer是可变的线程安全...\n\n");
        }
    }

    private void generateMiddleQuestions(String domain, StringBuilder questions) {
        questions.append("## 中级Java面试题\n\n");
        if ("jvm".equalsIgnoreCase(domain)) {
            questions.append("### JVM相关\n");
            questions.append("**问题**: 详细说明JVM内存模型和垃圾回收机制？\n");
            questions.append("**答案**: JVM内存分为堆、栈、方法区等，垃圾回收包括标记-清除、复制、标记-整理算法...\n\n");
        }
    }

    private void generateSeniorQuestions(String domain, StringBuilder questions) {
        questions.append("## 高级Java面试题\n\n");
        if ("spring".equalsIgnoreCase(domain)) {
            questions.append("### Spring框架深入\n");
            questions.append("**问题**: Spring的IoC容器启动流程是怎样的？\n");
            questions.append("**答案**: Spring容器启动包括BeanDefinition加载、Bean实例化、依赖注入、初始化等阶段...\n\n");
        }
    }

    /**
     * 代码优化分析
     */
    private void analyzeForOptimization(String code, StringBuilder optimization) {
        optimization.append("【优化建议】\n");

        // 集合优化
        if (code.contains("ArrayList")) {
            optimization.append("1. ArrayList优化：\n");
            optimization.append("   - 预设初始容量避免扩容：new ArrayList<>(expectedSize)\n");
            optimization.append("   - 考虑使用LinkedList适合频繁插入删除的场景\n\n");
        }

        // 字符串优化
        if (code.contains("String") && code.contains("+")) {
            optimization.append("2. 字符串优化：\n");
            optimization.append("   - 使用StringBuilder替代字符串连接\n");
            optimization.append("   - 考虑使用String.format()或MessageFormat\n\n");
        }

        // 循环优化
        if (code.contains("for") || code.contains("while")) {
            optimization.append("3. 循环优化：\n");
            optimization.append("   - 避免在循环中创建对象\n");
            optimization.append("   - 考虑使用Stream API提高可读性\n");
            optimization.append("   - 缓存数组长度避免重复计算\n\n");
        }

        // 并发优化
        if (code.contains("synchronized")) {
            optimization.append("4. 并发优化：\n");
            optimization.append("   - 减小锁的粒度\n");
            optimization.append("   - 考虑使用并发集合类\n");
            optimization.append("   - 使用ThreadLocal避免共享变量竞争\n\n");
        }
    }

    /**
     * 保存分析结果到文件
     *
     * @param fileName 文件名
     * @param content  文件内容
     */
    private void saveAnalysisResult(String fileName, String content) {
        try {
            // 构造文件路径并保存内容
            String filePath = FILE_DIR + "/" + fileName;
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
        } catch (Exception e) {
            // 忽略文件保存错误，不影响主要功能
        }
    }
}
