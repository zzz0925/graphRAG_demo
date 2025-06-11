package com.vector.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.vector.dto.vectorDto;
import com.vector.service.FileToGraphService;
import com.vector.service.doc2Vector;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vector")
@AllArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class doc2VectorController {
    private final doc2Vector doc2Vector;
    private final FileToGraphService fileToGraphService;
    @PostMapping("/doc2Vec")
    public ResponseEntity<Map<String, Object>> doc2Vector(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        log.info("doc2Vector called with text:{}....", fileName);
        doc2Vector.docVector(fileName);
        fileToGraphService.processFileToGraph(fileName);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "文件提取成功");
        response.put("fileName", fileName);

        return ResponseEntity.ok(response);
    }
}
