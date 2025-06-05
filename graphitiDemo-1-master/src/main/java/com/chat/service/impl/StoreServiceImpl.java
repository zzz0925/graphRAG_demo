package com.chat.service.impl;

import com.chat.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class StoreServiceImpl implements StoreService {
    @Override
    public boolean storeText(String message) {

        return false;
    }

    @Override
    public boolean storeVector(String message) {
        return false;
    }
}
