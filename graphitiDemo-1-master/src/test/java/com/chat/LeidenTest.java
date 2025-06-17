package com.chat;

import com.chat.dto.GraphData;
import com.chat.service.impl.GremlinServiceImpl;
import com.store.algorithm.LabelPropagation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class LeidenTest {

    @Autowired
    private GremlinServiceImpl gremlinServiceImpl;
    @Test
    public void testLeiden() {
        GraphData allGraphData = gremlinServiceImpl.getAllGraphData();
        List<Map<String, Object>> relationships = allGraphData.getRelationships();
        List<Map<String, Object>> nodes = allGraphData.getNodes();
        log.info("nodes size:{}, relationships size:{}", nodes.size(), relationships.size());
        LabelPropagation labelPropagation = new LabelPropagation();
        labelPropagation.init(nodes, relationships);
        labelPropagation.compute();
        System.out.println("Leiden Test Finished!");

    }
}
