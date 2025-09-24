package com.yam.myaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.yam.myaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Java面试题库工具
 * 提供各种Java面试题目的生成和管理功能
 * 
 * @author cocoyu
 * @date 2024-09-24
 */
public class JavaInterviewQuestionTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/interview-questions";

    @Tool(description = "随机生成Java面试题目，支持不同难度和技术领域")
    public String generateRandomQuestions(@ToolParam(description = "题目数量，默认5") int count,
                                        @ToolParam(description = "难度级别：easy/medium/hard") String difficulty,
                                        @ToolParam(description = "技术领域：basic/oop/collections/multithreading/jvm/spring/database") String category) {
        try {
            if (count <= 0) count = 5;
            if (count > 20) count = 20; // 限制最大题目数

            StringBuilder questions = new StringBuilder();
            questions.append("# Java面试题随机生成\n\n");
            questions.append("**生成时间**: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            questions.append("**难度级别**: ").append(difficulty).append("\n");
            questions.append("**技术领域**: ").append(category).append("\n");
            questions.append("**题目数量**: ").append(count).append("\n\n");
            questions.append("---\n\n");

            List<String> questionPool = getQuestionPool(difficulty, category);
            List<String> selectedQuestions = selectRandomQuestions(questionPool, count);

            for (int i = 0; i < selectedQuestions.size(); i++) {
                questions.append("## 题目 ").append(i + 1).append("\n\n");
                questions.append(selectedQuestions.get(i)).append("\n\n");
                questions.append("---\n\n");
            }

            // 保存题目到文件
            String fileName = String.format("java_interview_random_%s_%s_%s.md", 
                difficulty, category, System.currentTimeMillis());
            saveToFile(fileName, questions.toString());
            
            questions.append("题目已保存到文件: ").append(fileName);
            return questions.toString();

        } catch (Exception e) {
            return "生成面试题目失败: " + e.getMessage();
        }
    }

    @Tool(description = "创建Java面试模拟测试，包含计时和评分")
    public String createMockInterview(@ToolParam(description = "面试类型：technical/behavioral/mixed") String type,
                                    @ToolParam(description = "面试时长(分钟)，默认60") int duration) {
        try {
            if (duration <= 0) duration = 60;

            StringBuilder mockTest = new StringBuilder();
            mockTest.append("# Java面试模拟测试\n\n");
            mockTest.append("**测试类型**: ").append(type).append("\n");
            mockTest.append("**预计时长**: ").append(duration).append(" 分钟\n");
            mockTest.append("**开始时间**: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

            // 根据类型生成不同的面试内容
            if ("technical".equals(type)) {
                generateTechnicalInterview(mockTest, duration);
            } else if ("behavioral".equals(type)) {
                generateBehavioralInterview(mockTest, duration);
            } else {
                generateMixedInterview(mockTest, duration);
            }

            // 保存模拟测试
            String fileName = String.format("mock_interview_%s_%d_min_%s.md", 
                type, duration, System.currentTimeMillis());
            saveToFile(fileName, mockTest.toString());

            mockTest.append("\n\n📄 模拟面试已保存到文件: ").append(fileName);
            return mockTest.toString();

        } catch (Exception e) {
            return "创建模拟面试失败: " + e.getMessage();
        }
    }

    @Tool(description = "生成Java技术路线图和学习建议")
    public String generateLearningPath(@ToolParam(description = "当前技术水平：beginner/intermediate/advanced") String currentLevel,
                                     @ToolParam(description = "目标职位：junior/middle/senior/architect") String targetRole) {
        try {
            StringBuilder learningPath = new StringBuilder();
            learningPath.append("# Java技术学习路线图\n\n");
            learningPath.append("**当前水平**: ").append(currentLevel).append("\n");
            learningPath.append("**目标职位**: ").append(targetRole).append("\n");
            learningPath.append("**制定时间**: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");

            generateLearningPathContent(learningPath, currentLevel, targetRole);

            // 保存学习路线
            String fileName = String.format("java_learning_path_%s_to_%s_%s.md", 
                currentLevel, targetRole, System.currentTimeMillis());
            saveToFile(fileName, learningPath.toString());

            learningPath.append("\n\n📚 学习路线图已保存到文件: ").append(fileName);
            return learningPath.toString();

        } catch (Exception e) {
            return "生成学习路线失败: " + e.getMessage();
        }
    }

    /**
     * 获取题目池
     */
    private List<String> getQuestionPool(String difficulty, String category) {
        List<String> questions = new ArrayList<>();

        if ("basic".equals(category)) {
            questions.addAll(getBasicQuestions(difficulty));
        } else if ("oop".equals(category)) {
            questions.addAll(getOOPQuestions(difficulty));
        } else if ("collections".equals(category)) {
            questions.addAll(getCollectionQuestions(difficulty));
        } else if ("multithreading".equals(category)) {
            questions.addAll(getMultithreadingQuestions(difficulty));
        } else if ("jvm".equals(category)) {
            questions.addAll(getJVMQuestions(difficulty));
        } else if ("spring".equals(category)) {
            questions.addAll(getSpringQuestions(difficulty));
        } else {
            // 混合题目
            questions.addAll(getBasicQuestions(difficulty));
            questions.addAll(getOOPQuestions(difficulty));
            questions.addAll(getCollectionQuestions(difficulty));
        }

        return questions;
    }

    private List<String> getBasicQuestions(String difficulty) {
        List<String> questions = new ArrayList<>();
        
        if ("easy".equals(difficulty)) {
            questions.add("**问题**: Java中的基本数据类型有哪些？它们的默认值是什么？\n" +
                         "**考察点**: Java基础语法\n" +
                         "**参考答案**: 8种基本数据类型：byte(0)、short(0)、int(0)、long(0L)、float(0.0f)、double(0.0d)、boolean(false)、char('\\u0000')");
            
            questions.add("**问题**: == 和 equals() 的区别是什么？\n" +
                         "**考察点**: Java基础概念\n" +
                         "**参考答案**: ==比较的是引用地址，equals()比较的是内容。String等类重写了equals方法");
        } else if ("medium".equals(difficulty)) {
            questions.add("**问题**: 详细说明Java中的自动装箱和拆箱机制？\n" +
                         "**考察点**: Java类型转换\n" +
                         "**参考答案**: 自动装箱是基本类型自动转换为包装类，拆箱相反。编译器会自动调用valueOf()和xxxValue()方法");
        } else {
            questions.add("**问题**: Java中的反射机制是如何工作的？性能影响如何？\n" +
                         "**考察点**: 高级特性和性能\n" +
                         "**参考答案**: 反射通过Class对象获取类信息并动态调用，会有性能开销，应合理使用并考虑缓存");
        }
        
        return questions;
    }

    private List<String> getOOPQuestions(String difficulty) {
        List<String> questions = new ArrayList<>();
        
        if ("easy".equals(difficulty)) {
            questions.add("**问题**: 面向对象的三大特性是什么？请简单解释。\n" +
                         "**考察点**: OOP基础概念\n" +
                         "**参考答案**: 封装、继承、多态。封装隐藏内部实现，继承复用代码，多态实现接口统一");
        } else if ("medium".equals(difficulty)) {
            questions.add("**问题**: 抽象类和接口的区别？什么时候使用抽象类，什么时候使用接口？\n" +
                         "**考察点**: OOP设计原则\n" +
                         "**参考答案**: 抽象类可以有实现，接口只能有抽象方法(Java8后可有默认方法)。is-a关系用继承，can-do关系用接口");
        } else {
            questions.add("**问题**: 设计模式中的SOLID原则是什么？请举例说明如何在Java中应用。\n" +
                         "**考察点**: 高级设计原则\n" +
                         "**参考答案**: 单一职责、开闭原则、里氏替换、接口隔离、依赖倒置。通过具体代码示例说明每个原则的应用");
        }
        
        return questions;
    }

    private List<String> getCollectionQuestions(String difficulty) {
        List<String> questions = new ArrayList<>();
        
        questions.add("**问题**: ArrayList和LinkedList的区别？什么场景下使用哪个？\n" +
                     "**考察点**: 集合框架\n" +
                     "**参考答案**: ArrayList基于数组，随机访问快；LinkedList基于链表，插入删除快。根据操作特点选择");
        
        return questions;
    }

    private List<String> getMultithreadingQuestions(String difficulty) {
        List<String> questions = new ArrayList<>();
        
        questions.add("**问题**: synchronized和ReentrantLock的区别？\n" +
                     "**考察点**: 并发编程\n" +
                     "**参考答案**: synchronized是关键字，ReentrantLock是类。后者提供更多功能如公平锁、条件变量等");
        
        return questions;
    }

    private List<String> getJVMQuestions(String difficulty) {
        List<String> questions = new ArrayList<>();
        
        questions.add("**问题**: JVM内存模型包含哪些区域？各自的作用是什么？\n" +
                     "**考察点**: JVM原理\n" +
                     "**参考答案**: 堆、栈、方法区、PC寄存器、本地方法栈。每个区域存储不同类型的数据");
        
        return questions;
    }

    private List<String> getSpringQuestions(String difficulty) {
        List<String> questions = new ArrayList<>();
        
        questions.add("**问题**: Spring的IoC和AOP是什么？如何实现的？\n" +
                     "**考察点**: Spring框架\n" +
                     "**参考答案**: IoC控制反转通过依赖注入实现，AOP面向切面编程通过代理模式实现");
        
        return questions;
    }

    /**
     * 随机选择题目
     */
    private List<String> selectRandomQuestions(List<String> questionPool, int count) {
        List<String> selected = new ArrayList<>();
        List<String> temp = new ArrayList<>(questionPool);
        
        for (int i = 0; i < Math.min(count, temp.size()); i++) {
            int randomIndex = RandomUtil.randomInt(temp.size());
            selected.add(temp.remove(randomIndex));
        }
        
        return selected;
    }

    /**
     * 生成技术面试内容
     */
    private void generateTechnicalInterview(StringBuilder mockTest, int duration) {
        mockTest.append("## 技术面试流程\n\n");
        mockTest.append("### 第一部分：Java基础 (").append(duration * 0.3).append(" 分钟)\n");
        mockTest.append("- 面向对象特性\n");
        mockTest.append("- 集合框架使用\n");
        mockTest.append("- 异常处理机制\n\n");
        
        mockTest.append("### 第二部分：框架应用 (").append(duration * 0.4).append(" 分钟)\n");
        mockTest.append("- Spring框架原理\n");
        mockTest.append("- 数据库操作\n");
        mockTest.append("- 项目实战经验\n\n");
        
        mockTest.append("### 第三部分：系统设计 (").append(duration * 0.3).append(" 分钟)\n");
        mockTest.append("- 架构设计思路\n");
        mockTest.append("- 性能优化方案\n");
        mockTest.append("- 问题解决能力\n\n");
    }

    /**
     * 生成行为面试内容
     */
    private void generateBehavioralInterview(StringBuilder mockTest, int duration) {
        mockTest.append("## 行为面试问题\n\n");
        mockTest.append("1. 请介绍一个你在项目中遇到的最大技术挑战，以及你是如何解决的？\n");
        mockTest.append("2. 描述一次你与团队成员意见不一致的情况，最后是如何处理的？\n");
        mockTest.append("3. 你如何保持技术的持续学习？最近学习了哪些新技术？\n");
        mockTest.append("4. 在项目进度紧张的情况下，你如何平衡代码质量和交付时间？\n");
        mockTest.append("5. 描述一次你主动优化系统性能的经历？\n\n");
    }

    /**
     * 生成混合面试内容
     */
    private void generateMixedInterview(StringBuilder mockTest, int duration) {
        generateTechnicalInterview(mockTest, duration / 2);
        generateBehavioralInterview(mockTest, duration / 2);
    }

    /**
     * 生成学习路线内容
     */
    private void generateLearningPathContent(StringBuilder learningPath, String currentLevel, String targetRole) {
        learningPath.append("## 学习阶段规划\n\n");
        
        if ("beginner".equals(currentLevel)) {
            learningPath.append("### 第一阶段：Java基础 (2-3个月)\n");
            learningPath.append("- [ ] Java语法基础\n");
            learningPath.append("- [ ] 面向对象编程\n");
            learningPath.append("- [ ] 集合框架\n");
            learningPath.append("- [ ] IO操作\n");
            learningPath.append("- [ ] 异常处理\n\n");
        }
        
        learningPath.append("### 进阶学习：框架技术 (3-4个月)\n");
        learningPath.append("- [ ] Spring Framework\n");
        learningPath.append("- [ ] Spring Boot\n");
        learningPath.append("- [ ] MyBatis/JPA\n");
        learningPath.append("- [ ] MySQL数据库\n");
        learningPath.append("- [ ] Redis缓存\n\n");
        
        if ("architect".equals(targetRole)) {
            learningPath.append("### 高级技能：架构设计 (6个月以上)\n");
            learningPath.append("- [ ] 微服务架构\n");
            learningPath.append("- [ ] 分布式系统\n");
            learningPath.append("- [ ] 系统设计\n");
            learningPath.append("- [ ] 性能调优\n");
            learningPath.append("- [ ] 团队管理\n\n");
        }
        
        learningPath.append("## 推荐学习资源\n\n");
        learningPath.append("### 书籍推荐\n");
        learningPath.append("- 《Java核心技术》\n");
        learningPath.append("- 《Effective Java》\n");
        learningPath.append("- 《Spring实战》\n");
        learningPath.append("- 《深入理解Java虚拟机》\n\n");
        
        learningPath.append("### 实践项目建议\n");
        learningPath.append("- 博客系统\n");
        learningPath.append("- 电商平台\n");
        learningPath.append("- 秒杀系统\n");
        learningPath.append("- 分布式任务调度\n");
    }

    /**
     * 保存内容到文件
     */
    private void saveToFile(String fileName, String content) {
        try {
            String filePath = FILE_DIR + "/" + fileName;
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
        } catch (Exception e) {
            // 忽略文件保存错误
        }
    }
}
