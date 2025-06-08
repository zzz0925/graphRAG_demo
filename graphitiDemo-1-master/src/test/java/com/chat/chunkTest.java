package com.chat;

import com.vector.service.textChunk;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class chunkTest {
    @Autowired
    private textChunk textChunk;
    @Test
    public void test() {
        System.out.println("chunkTest");
        textChunk.chunk("book.txt");
    }
}
