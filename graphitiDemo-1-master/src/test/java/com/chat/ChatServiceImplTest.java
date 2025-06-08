package com.chat;

import com.chat.config.promptConfig;
import com.chat.service.impl.ChatServiceImpl;
import com.chat.service.impl.ModelServiceImpl;
import com.chat.service.impl.Neo4jServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class ChatServiceImplTest {

    @Autowired
    private ChatServiceImpl chatServiceImpl;
    @Autowired
    private ModelServiceImpl modelServiceImpl;
    @Autowired
    private Neo4jServiceImpl neo4JServiceImpl;
    @Test
    public void test() {
        //TODO：将该轮对话传入大模型，提取出实体和关系，并更新到neo4j数据库中
        String query = "user: " + "我是zzz，目前居住在杭州，你是谁" + " assistant: " + "我是阿里开发的通义千问大模型";
        String prompt = promptConfig.configPrompt("MERGE",query);
        String cypher = modelServiceImpl.generateResponse(prompt);
        log.info(cypher);
        //TODO：将CypherList中的语句逐个执行，更新到neo4j数据库中
        neo4JServiceImpl.createNode(cypher);
//        if (neo4jService.createNode(cypher)) {
//            log.info("成功创建节点");
//        }else {
//            log.info("创建节点失败");
//        }
    }
}
