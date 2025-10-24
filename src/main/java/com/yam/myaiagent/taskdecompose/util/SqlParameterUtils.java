package com.yam.myaiagent.taskdecompose.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SQL参数处理工具类
 * 用于识别和格式化SQL参数，特别是处理IN子句参数
 */
@Slf4j
public class SqlParameterUtils {

    /**
     * 参数类型枚举
     */
    public enum ParameterType {
        STRING,     // 字符串类型
        NUMBER,     // 数字类型
        BOOLEAN,    // 布尔类型
        DATE,       // 日期类型
        LIST,       // 列表类型（用于IN子句）
        UNKNOWN     // 未知类型
    }

    // 数字正则表达式
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    
    // 布尔值正则表达式
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
    
    // 日期正则表达式 (YYYY-MM-DD)
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    
    // 带引号的日期正则表达式 ('YYYY-MM-DD')
    private static final Pattern QUOTED_DATE_PATTERN = Pattern.compile("^'\\d{4}-\\d{2}-\\d{2}'$");
    
    // IN子句列表正则表达式 - 支持数字和日期格式
    private static final Pattern LIST_PATTERN = Pattern.compile("^\\s*(\\d+|\\d{4}-\\d{2}-\\d{2})\\s*(,\\s*(\\d+|\\d{4}-\\d{2}-\\d{2})\\s*)*$");

    /**
     * 识别参数类型
     *
     * @param value 参数值
     * @return 参数类型
     */
    public static ParameterType identifyParameterType(Object value) {
        if (value == null) {
            return ParameterType.UNKNOWN;
        }

        String strValue = value.toString().trim();
        
        // 检查是否为空
        if (StringUtils.isEmpty(strValue)) {
            return ParameterType.STRING;
        }
        
        // 检查是否为数字
        if (NUMBER_PATTERN.matcher(strValue).matches()) {
            return ParameterType.NUMBER;
        }
        
        // 检查是否为布尔值
        if (BOOLEAN_PATTERN.matcher(strValue).matches()) {
            return ParameterType.BOOLEAN;
        }
        
        // 检查是否为日期（带引号或不带引号）
        if (DATE_PATTERN.matcher(strValue).matches() || QUOTED_DATE_PATTERN.matcher(strValue).matches()) {
            return ParameterType.DATE;
        }
        
        // 检查是否为列表（用于IN子句）
        if (LIST_PATTERN.matcher(strValue).matches()) {
            return ParameterType.LIST;
        }
        
        // 默认为字符串类型
        return ParameterType.STRING;
    }

    /**
     * 格式化参数值，根据参数类型添加适当的引号或其他格式化处理
     *
     * @param value 参数值
     * @param type 参数类型
     * @return 格式化后的参数值
     */
    public static String formatParameterValue(Object value, ParameterType type) {
        if (value == null) {
            return "null";
        }

        String strValue = value.toString().trim();
        
        switch (type) {
            case STRING:
                return "'" + escapeSqlString(strValue) + "'";
            case NUMBER:
                return strValue;
            case BOOLEAN:
                return strValue.toLowerCase();
            case DATE:
                // 检查日期是否已经包含单引号
                if (strValue.startsWith("'") && strValue.endsWith("'")) {
                    return strValue; // 已经有单引号，不再添加
                } else {
                    return "'" + strValue + "'";
                }
            case LIST:
                return formatListParameter(strValue);
            case UNKNOWN:
            default:
                // 对于未知类型，默认作为字符串处理
                return "'" + escapeSqlString(strValue) + "'";
        }
    }

    /**
     * 格式化参数值，自动识别参数类型
     *
     * @param value 参数值
     * @return 格式化后的参数值
     */
    public static String formatParameterValue(Object value) {
        ParameterType type = identifyParameterType(value);
        return formatParameterValue(value, type);
    }

    /**
     * 格式化IN子句参数
     *
     * @param listValue 逗号分隔的列表值
     * @return 格式化后的IN子句参数
     */
    public static String formatListParameter(String listValue) {
        if (StringUtils.isEmpty(listValue)) {
            log.debug("【列表格式化】值为空，返回默认值 ''");
            return "''";
        }

        log.info("【列表格式化】开始格式化列表参数: '{}'", listValue);

        // 分割列表值，处理可能的引号问题
        String[] items;
        if (listValue.contains("'")) {
            // 使用正则表达式处理带引号的列表
            List<String> extractedItems = new ArrayList<>();
            Pattern pattern = Pattern.compile("'([^']*)'|([^,]+)");
            Matcher matcher = pattern.matcher(listValue);
            
            while (matcher.find()) {
                // 优先使用引号内的内容，如果没有则使用非逗号内容
                String item = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (item != null && !item.trim().isEmpty()) {
                    extractedItems.add(item.trim());
                }
            }
            
            items = extractedItems.toArray(new String[0]);
        } else {
            // 普通分割
            items = listValue.split(",");
        }
        
        log.info("【列表格式化】分割后得到{}个项", items.length);
        List<String> formattedItems = new ArrayList<>();
        
        // 处理每个项
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            String trimmedItem = item.trim();
            log.debug("【列表格式化】处理第{}项: '{}'", i+1, trimmedItem);
            
            // 移除可能存在的多余引号
            if (trimmedItem.startsWith("'") && trimmedItem.endsWith("'")) {
                // 已经有单引号，直接使用
                formattedItems.add(trimmedItem);
                log.debug("【列表格式化】项{}已有引号，保持不变: '{}'", i+1, trimmedItem);
            } else {
                // 没有单引号，根据类型添加
                ParameterType itemType = identifyParameterType(trimmedItem);
                String formattedItem = formatParameterValue(trimmedItem, itemType);
                formattedItems.add(formattedItem);
                log.debug("【列表格式化】项{}格式化后: '{}' (类型: {})", i+1, formattedItem, itemType);
            }
        }
        
        // 组合成IN子句参数
        String result = String.join(",", formattedItems);
        log.info("【列表格式化】最终格式化结果: '{}'", result);
        return result;
    }

    /**
     * 转义SQL字符串中的特殊字符
     *
     * @param value SQL字符串
     * @return 转义后的字符串
     */
    public static String escapeSqlString(String value) {
        if (value == null) {
            return "";
        }
        // 替换单引号为两个单引号（SQL标准转义方式）
        return value.replace("'", "''");
    }

    /**
     * 检查SQL参数是否安全（防止SQL注入）
     *
     * @param value 参数值
     * @return 是否安全
     */
    public static boolean isSqlParameterSafe(String value) {
        if (value == null) {
            return true;
        }
        
        // 检查是否包含可能导致SQL注入的字符
        String[] dangerousKeywords = {
            ";", "--", "/*", "*/", "@@", "@", "char", "nchar", "varchar", "nvarchar",
            "alter", "begin", "cast", "create", "cursor", "declare", "delete",
            "drop", "end", "exec", "execute", "fetch", "insert", "kill",
            "open", "select", "sys", "sysobjects", "syscolumns", "table",
            "update", "xp_"
        };
        
        String lowerValue = value.toLowerCase();
        for (String keyword : dangerousKeywords) {
            if (lowerValue.contains(keyword)) {
                log.warn("SQL参数包含潜在危险关键字: {}", keyword);
                return false;
            }
        }
        
        return true;
    }

    /**
     * 处理IN子句参数，将字符串转换为适当格式的IN子句参数列表
     *
     * @param value IN子句参数值
     * @return 处理后的IN子句参数
     */
    public static String processInClauseParameter(String value) {
        if (StringUtils.isEmpty(value)) {
            log.debug("【IN子句处理】值为空，返回默认值 ''");
            return "''";
        }

        log.info("【IN子句处理】开始处理IN子句参数: '{}'", value);

        // 移除括号
        String cleanValue = value.trim();
        if (cleanValue.startsWith("(") && cleanValue.endsWith(")")) {
            cleanValue = cleanValue.substring(1, cleanValue.length() - 1);
            log.info("【IN子句处理】移除括号后: '{}'", cleanValue);
        }
        
        // 处理可能的引号问题
        if (cleanValue.contains("'")) {
            log.info("【IN子句处理】检测到包含单引号的IN子句");
            
            // 使用正则表达式匹配所有被单引号包围的值
            List<String> quotedValues = new ArrayList<>();
            Pattern pattern = Pattern.compile("'([^']*)'");
            Matcher matcher = pattern.matcher(cleanValue);
            
            while (matcher.find()) {
                quotedValues.add(matcher.group(1));
            }
            
            if (!quotedValues.isEmpty()) {
                log.info("【IN子句处理】提取到{}个带引号的值: {}", quotedValues.size(), quotedValues);
                // 将提取的值重新组合，不带引号
                cleanValue = String.join(",", quotedValues);
                log.info("【IN子句处理】重新组合后的值: '{}'", cleanValue);
            }
        }
        
        // 分割并处理每个项
        String result = formatListParameter(cleanValue);
        log.info("【IN子句处理】处理后的IN子句参数: '{}'", result);
        return result;
    }

    /**
     * 判断参数是否为IN子句参数
     *
     * @param paramName 参数名
     * @param value 参数值
     * @return 是否为IN子句参数
     */
    public static boolean isInClauseParameter(String paramName, Object value) {
        if (value == null) {
            log.debug("【IN子句检测】值为null，不是IN子句参数");
            return false;
        }
        
        // 通过参数名判断
        if (paramName != null && paramName.toLowerCase().contains("in")) {
            log.info("【IN子句检测】参数名 '{}' 包含'in'，判定为IN子句参数", paramName);
            return true;
        }
        
        // 通过值判断
        String strValue = value.toString().trim();
        boolean isListPattern = LIST_PATTERN.matcher(strValue).matches();
        boolean isParenthesisPattern = (strValue.startsWith("(") && strValue.endsWith(")") && strValue.contains(","));
        
        if (isListPattern) {
            log.info("【IN子句检测】值 '{}' 匹配LIST_PATTERN，判定为IN子句参数", strValue);
        }
        
        if (isParenthesisPattern) {
            log.info("【IN子句检测】值 '{}' 匹配括号模式，判定为IN子句参数", strValue);
        }
        
        return isListPattern || isParenthesisPattern;
    }
}