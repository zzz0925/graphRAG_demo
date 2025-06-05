package com.store.entiry;

import lombok.Data;

@Data
public class ZhipuResult {
    private int code;
    private String msg;
    private boolean success;
    private EmbeddingResult data;
}