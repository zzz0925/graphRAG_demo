package com.chat;

import com.chat.dto.GraphData;
import com.chat.service.impl.GremlinServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GremlinTest {
    @Autowired
    private GremlinServiceImpl gremlinService;
    @Test
    public void testGremlin() {
        GraphData allGraphData = gremlinService.getAllGraphData();
        if(allGraphData!= null){
            System.out.println(allGraphData.toString());
        }
    }
}
