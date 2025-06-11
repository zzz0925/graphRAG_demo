package com.chat.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class GremlinProcessor {

    // 常见的 Gremlin 关键词模式
    private static final Pattern GREMLIN_PATTERN = Pattern.compile(
            "^\\s*(g\\.|addV\\(|addE\\(|V\\(|E\\(|has\\(|property\\(|from\\(|to\\(|drop\\()",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 处理模型返回的文本，提取并清理 Gremlin 语句
     */
    public static List<String> processGremlinResponse(String modelResponse) {
        if (!StringUtils.hasText(modelResponse)) {
            log.warn("模型返回内容为空");
            return new ArrayList<>();
        }

        log.info("原始模型返回: {}", modelResponse);

        // 1. 先处理多行语句，将反斜杠换行转换为单行
        String cleanedResponse = modelResponse
                .replaceAll("\\\\\\s*\\n", " ")     // 替换 \换行 为空格
                .replaceAll("\\\\\\s*\\r", " ")     // 替换 \回车 为空格
                .replaceAll("\\\\", " ")            // 移除单独的反斜杠
                .replaceAll("\\s+", " ");           // 多个空格合并为一个

        // 2. 按分号分割语句
        List<String> statements = Arrays.stream(cleanedResponse.split(";"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        // 3. 过滤和清理语句
        List<String> validStatements = new ArrayList<>();

        for (String statement : statements) {
            String cleanedStatement = cleanGremlinStatement(statement);
            if (isValidGremlinStatement(cleanedStatement)) {
                validStatements.add(cleanedStatement);
            } else {
                log.warn("过滤掉无效语句: {}", statement);
            }
        }

        log.info("处理后的有效 Gremlin 语句数量: {}", validStatements.size());
        validStatements.forEach(stmt -> log.info("有效 Gremlin 语句: {}", stmt));

        return validStatements;
    }

    /**
     * 清理单个 Gremlin 语句
     */
    private static String cleanGremlinStatement(String statement) {
        if (!StringUtils.hasText(statement)) {
            return "";
        }

        // 移除代码块标记
        statement = statement.replaceAll("```[a-zA-Z]*", "").trim();

        // 移除换行符和反斜杠 - 这是关键修复
        statement = statement.replaceAll("\\\\\\s*\\n", "")  // 移除 \换行
                .replaceAll("\\\\\\s*\\r", "")  // 移除 \回车
                .replaceAll("\\\\", "")         // 移除单独的反斜杠
                .replaceAll("\\s*\\n\\s*", " ") // 将换行替换为空格
                .replaceAll("\\s*\\r\\s*", " ") // 将回车替换为空格
                .replaceAll("\\s+", " ");       // 多个空格合并为一个

        // 移除常见的中文说明文字
        statement = statement.replaceAll("^[^g]*?(g\\.)", "$1");

        // 移除行末的中文注释
        statement = statement.replaceAll("//.*$", "").trim();

        // 移除分号（因为 Gremlin 不需要分号结尾）
        statement = statement.replaceAll(";+$", "").trim();

        return statement.trim();
    }

    /**
     * 验证是否为有效的 Gremlin 语句
     */
    private static boolean isValidGremlinStatement(String statement) {
        if (!StringUtils.hasText(statement)) {
            return false;
        }

        // 检查是否符合 Gremlin 语法模式
        return GREMLIN_PATTERN.matcher(statement).find();
    }
}