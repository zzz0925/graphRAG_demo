package com.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class cypherSplit {

    @Test
    public void test()
    {
        String cypher = "MATCH (n) WHERE n.name = 'Alice' RETURN n;Match (n) WHERE n.name = 'Bob' RETURN n;";
        String[] cyphers = cypher.split("(?<=;)");
        for (String c : cyphers) {
            System.out.println(c);
        }
    }
}
