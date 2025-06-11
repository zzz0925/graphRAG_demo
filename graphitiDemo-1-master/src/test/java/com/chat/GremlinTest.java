package com.chat;

import com.chat.dto.GraphData;
import com.chat.service.impl.GremlinServiceImpl;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GremlinTest {
    @Autowired
    private GremlinServiceImpl gremlinService;
    @Autowired
    private Client gremlinClient;
    @Test
    public void testGremlin() {
        GraphData allGraphData = gremlinService.getAllGraphData();
        if(allGraphData!= null){
            System.out.println(allGraphData.toString());
        }
    }
    @Test
    public void testAdd(){
        String gremlin = """
                g.addV('entity2').
                    property(id, 'unique-entity-id2').
                    property('name', 'EntityName').
                    property('createdDate', '2025-06-11')""";
        ResultSet resultSet1 = gremlinClient.submit(gremlin);
        String query = "g.V('unique-entity-id')";
        ResultSet resultSet = gremlinClient.submit("g.V().has('name', 'EntityName')");

        System.out.println("resultSet1.all().toString() = "+resultSet.all().toString());
    }
}
