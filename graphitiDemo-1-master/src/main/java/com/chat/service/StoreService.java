package com.chat.service;

public interface StoreService {
    boolean storeText(String message);
    boolean storeVector(String message);
}
