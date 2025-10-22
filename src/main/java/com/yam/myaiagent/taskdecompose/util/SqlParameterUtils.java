package com.yam.myaiagent.taskdecompose.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    
    // IN子句列表正则表达式
    private static final Pattern LIST_PATTERN = Pattern.compile("^\\s*\\d+\\s*(,\\s*\\d+\\s*)*$");

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
        
        // 检查是否为日期
        if (DATE_PATTERN.matcher(strValue).matches()) {
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
                return "'" + strValue + "'";
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
            return "''";
        }

        // 分割列表值
        String[] items = listValue.split(",");
        List<String> formattedItems = new ArrayList<>();
        
        // 处理每个项
        for (String item : items) {
            String trimmedItem = item.trim();
            ParameterType itemType = identifyParameterType(trimmedItem);
            formattedItems.add(formatParameterValue(trimmedItem, itemType));
        }
        
        // 组合成IN子句参数
        return String.join(",", formattedItems);
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
            return "''";
        }

        // 移除括号
        String cleanValue = value.trim();
        if (cleanValue.startsWith("(") && cleanValue.endsWith(")")) {
            cleanValue = cleanValue.substring(1, cleanValue.length() - 1);
        }
        
        // 分割并处理每个项
        return formatListParameter(cleanValue);
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
            return false;
        }
        
        // 通过参数名判断
        if (paramName != null && paramName.toLowerCase().contains("in")) {
            return true;
        }
        
        // 通过值判断
        String strValue = value.toString().trim();
        return LIST_PATTERN.matcher(strValue).matches() || 
               (strValue.startsWith("(") && strValue.endsWith(")") && strValue.contains(","));
    }
}