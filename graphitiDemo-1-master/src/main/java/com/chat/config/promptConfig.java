package com.chat.config;

public class promptConfig {
    public static String promptTemplate = "你是一位专业的自然语言理解助手，擅长从对话中提取关键信息。你的任务是从以下对话中识别出所有提到的 实体（Entity） 和它们之间的 关系（Relationship）。\n" +
            "用户问题如下：\n" +
            "%s\n" +
            "要求如下：\n" +
            "\n" +
            "提取实体：识别并列出对话中出现的所有实体（如人名、地点、组织、时间、事件、物品等）。每个实体应具有明确的类型（Label），例如 Person、Location、Organization 等。\n" +
            "识别关系：识别实体之间存在的关系，每种关系应有明确的关系类型（Relation Type），并指明方向。\n" +
            "生成 Cypher 语句：根据上述提取的实体与关系，生成一组 Neo4j 的 Cypher 查询语句，用于在图数据库中创建节点和关系。\n" +
            "格式要求：\n" +
            "Cypher 语句应使用 MERGE 避免重复插入，格式清晰、可执行。\n" +
            "请只回复cypher语句！！不要有任何其他语句！只输出Cypher查询的结构化结果！！！";
    public static String configPrompt(String prompt) {
        return String.format(promptTemplate, prompt);
    }

}
