package com.chat.service;

public interface QueryService {
    public String queryNeo(String query);
    public String queryGdb(String query);
    public String queryDb(String query);
}
