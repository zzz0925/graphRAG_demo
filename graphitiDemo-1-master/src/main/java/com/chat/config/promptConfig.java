package com.chat.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class promptConfig {
    public static String MERGE_Gremlin = "MERGE";
    public static String QUERY_Gremlin = "QUERY";
    public static String RESPONSE = "RESPONSE";
    public static String mergerPromptTemplate = "你是一位专业的自然语言理解助手，擅长从对话中提取关键信息。你的任务是从以下对话中识别出所有提到的 实体（Entity） 和它们之间的 关系（Relationship）。\n" +
            "用户问题如下：\n" +
            "%s\n" +
            "要求如下：\n" +
            "\n" +
            "提取实体：识别并列出对话中出现的所有实体（如人名、地点、组织、时间、事件、物品等）。每个实体应具有明确的类型（Label），例如 Person、Location、Organization 等。\n" +
            "识别关系：识别实体之间存在的关系，每种关系应有明确的关系类型（Relation Type），并指明方向。\n" +
            "生成 Gremlin 语句：根据上述提取的实体与关系，生成一组Gremlin 查询语句，用于在图数据库中创建节点和关系。\n" +
            "格式要求：\n" +
            "Gremlin 语句应避免重复插入，格式清晰、可执行，标签要做为节点的类型;\n" +
            "请只回复 Gremlin 语句！！不要有任何其他语句！只输出 Gremlin 查询的结构化结果，每条Gremlin，语句用分号隔开！！！";
    public static String queryPromptTemplate = "你是一位专业的图数据库查询助手，擅长从图数据库中查询数据。你的任务是从以下图数据库中查询出所有关于 实体（Entity） 和它们之间的 关系（Relationship）的信息。\n" +
            "用户问题如下：\n" +
            "%s\n" +
            "要求如下：\n" +
            "\n" +
            "查询实体：根据用户问题，提取出实体的名称，查询图数据库中符合条件的实体。\n" +
            "查询关系：根据提取出的实体查找该实体所有的关系。\n" +
            "生成 Gremlin 语句：根据上述查询条件，生成一组 Gremlin 查询语句，用实体的名称进行查询，不用标签，尽量避免返回整个图谱。\n" +
            "格式要求：\n" +
            "务必包含对提取出的单一节点的查询，关系用中文表示，Gremlin 语句应格式清晰、可执行。并且返回的Gremlin语句要能够直接传入图数据库进行查询！！\n" +
            "请只回复Gremlin语句！！不要有任何其他语句！只输出Gremlin查询的结构化结果，每条Gremlin，语句用分号隔开！！！";
    public static String responseTemplate = "请利用如下上下文的信息回答问题：" + "\n" +
            "%s\n" +
            "上下文信息如下：%s" + "\n" +
            "%s\n" +
            "如果上下文存在冲突，根据时间来判断选择哪个上下文。如果上下文信息中没有帮助,则不允许胡乱回答！";
    public static String configPrompt(String Operation,String prompt) {
        if (MERGE_Gremlin.equals(Operation)) {
            return String.format(mergerPromptTemplate, prompt);
        }else if(QUERY_Gremlin.equals(Operation)) {
            return String.format(queryPromptTemplate, prompt);
        }else if(RESPONSE.equals(Operation)){
            return String.format(responseTemplate, prompt);
        }else {
            return "请输入正确的操作类型！";
        }
    }

}
