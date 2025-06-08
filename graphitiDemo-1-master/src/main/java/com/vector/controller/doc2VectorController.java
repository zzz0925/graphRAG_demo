package com.vector.controller;

import com.vector.dto.vectorDto;
import com.vector.service.doc2Vector;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/vector")
@AllArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class doc2VectorController {
    private final doc2Vector doc2Vector;

    @PostMapping("/doc2Vec")
    public String doc2Vector(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        log.info("doc2Vector called with text:{}....", fileName);
        return doc2Vector.docVector(fileName);
    }
}
