package com.chat.service.impl;

import com.chat.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class QueryServiceImpl implements QueryService {

    @Override
    public String queryNeo(String query) {
        //需要生成对应的cypher语句

        return "";
    }

    @Override
    public String queryGdb(String query) {
        return "";
    }

    @Override
    public String queryDb(String query) {
        //通过语义检索

        return "";
    }
}
