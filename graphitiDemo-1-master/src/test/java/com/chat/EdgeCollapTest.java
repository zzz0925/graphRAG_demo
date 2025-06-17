package com.chat;

import com.chat.config.promptConfig;
import com.chat.service.impl.GremlinServiceImpl;
import com.chat.service.impl.ModelServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EdgeCollapTest {
    @Autowired
    private GremlinServiceImpl  gremlinServiceImpl;
    @Autowired
    private ModelServiceImpl modelServiceImpl;
    @Test
    public void test(){

        String result = "用户问题：zzz喜欢什么？" + "图谱检索结果：" + gremlinServiceImpl.GremlinQuery("g.V().has('name', 'zzz').bothE().project('label', 'time').by(label()).by('valid_time')");
        String response = modelServiceImpl.generateResponse(result);
        System.out.println(response);
    }
}
