package com.store.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.store.entiry.ChunkResult;
import com.store.entiry.EmbeddingResult;
import com.store.entiry.ZhipuResult;
import com.store.repository.VectorStorage;
import com.store.service.VectorStore;
import com.store.utils.LLMUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
@Slf4j
@Service
public class VectorStoreImpl implements VectorStore {

    @Autowired
    private VectorStorage vectorStorage;

    final Gson GSON=new Gson();

    @Value("${llm.zp-key}")
    private String zpKey;
    @Value("${llm.zp-url}")
    private String zpUrl;
    @Override
    public List<ChunkResult> chunk(String doc) {
        if(doc.endsWith(".pdf")){
            return chunkPDF(doc);
        }else if(doc.endsWith(".txt")){
            return chunkTxt(doc);
        }else {
            log.error("不支持的文件类型: {}", doc);
        }
        return new ArrayList<>();
    }

    public List<ChunkResult> chunkPDF(String doc){
        String path = "data/" + doc;
        log.info("start chunk---> doc:{}, path:{}", doc, path);
        ClassPathResource classPathResource = new ClassPathResource(path);
        try {
            // 利用 PDFBox 读取 PDF 文档
            RandomAccessReadBuffer rarBuffer = new RandomAccessReadBuffer(IOUtils.toByteArray(classPathResource.getInputStream()));
            PDDocument document = Loader.loadPDF(rarBuffer);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            // 按固定字数分割，例如每256个字符一块
            String[] chunks = StrUtil.split(text, 256);
            log.info("chunk size: {}", ArrayUtil.length(chunks));
            List<ChunkResult> results = new ArrayList<>();
            AtomicInteger atomicInteger = new AtomicInteger(0);
            for (String chunk : chunks) {
                ChunkResult chunkResult = new ChunkResult();
                chunkResult.setDocId(doc);
                chunkResult.setContent(chunk);
                chunkResult.setChunkId(atomicInteger.incrementAndGet());
                results.add(chunkResult);
            }
            return results;
        } catch (IOException e) {
            log.error("PDF 分块错误: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<ChunkResult> chunkTxt(String docId){
        String path="data/"+docId;
        log.info("start chunk---> docId:{},path:{}",docId,path);
        ClassPathResource classPathResource=new ClassPathResource(path);
        try {
            String txt= IoUtil.read(classPathResource.getInputStream(), StandardCharsets.UTF_8);
            //按固定字数分割,256
            String[] lines=StrUtil.split(txt,256);
            log.info("chunk size:{}", ArrayUtil.length(lines));
            List<ChunkResult> results=new ArrayList<>();
            AtomicInteger atomicInteger=new AtomicInteger(0);
            for (String line:lines){
                ChunkResult chunkResult=new ChunkResult();
                chunkResult.setDocId(docId);
                chunkResult.setContent(line);
                chunkResult.setChunkId(atomicInteger.incrementAndGet());
                results.add(chunkResult);
            }
            return results;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<EmbeddingResult> embedding(List<ChunkResult> ChunkResults) {
        log.info("start embedding,size:{}", CollectionUtil.size(ChunkResults));
        if (CollectionUtil.isEmpty(ChunkResults)){
            return new ArrayList<>();
        }
        List<EmbeddingResult> EmbeddingResults =new ArrayList<>();
        for (ChunkResult chunkResult: ChunkResults){
            EmbeddingResults.add(this.embedding(chunkResult));
        }
        return EmbeddingResults;
    }

    public EmbeddingResult embedding(ChunkResult chunkResult){
        log.info("zp-key:{}",zpKey);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS)
                .addInterceptor(new ZhipuHeaderInterceptor(zpKey));
        OkHttpClient okHttpClient = builder.build();
        EmbeddingResult embedRequest=new EmbeddingResult();
        embedRequest.setPrompt(chunkResult.getContent());
        embedRequest.setRequestId(Objects.toString(chunkResult.getChunkId()));
        // 智谱embedding
        Request request = new Request.Builder()
                .url(zpUrl)
                .post(RequestBody.create(GSON.toJson(embedRequest), MediaType.get(ContentType.JSON.getValue())))
                .build();
        try {
            Response response= okHttpClient.newCall(request).execute();
            String result=response.body().string();
            System.out.println("result: "+result);
            ZhipuResult zhipuResult= GSON.fromJson(result, ZhipuResult.class);
            EmbeddingResult ret= zhipuResult.getData();
            ret.setPrompt(embedRequest.getPrompt());
            ret.setRequestId(embedRequest.getRequestId());
            return  ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public EmbeddingResult embedding(String sentence){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS)
                .addInterceptor(new ZhipuHeaderInterceptor(zpKey));
        OkHttpClient okHttpClient = builder.build();
        EmbeddingResult embedRequest=new EmbeddingResult();
        embedRequest.setPrompt(sentence);// 智谱embedding
        embedRequest.setRequestId(RandomUtil.randomString(8));
        Request request = new Request.Builder()
                .url(zpUrl)
                .post(RequestBody.create(GSON.toJson(embedRequest), MediaType.get(ContentType.JSON.getValue())))
                .build();
        try {
            Response response= okHttpClient.newCall(request).execute();
            String result=response.body().string();
            System.out.println("result: "+result);
            ZhipuResult zhipuResult= GSON.fromJson(result, ZhipuResult.class);
            EmbeddingResult ret= zhipuResult.getData();
            ret.setPrompt(embedRequest.getPrompt());
            ret.setRequestId(embedRequest.getRequestId());
            return  ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @AllArgsConstructor
    private static class ZhipuHeaderInterceptor implements Interceptor {

        final String apiKey;

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request original = chain.request();
            String authorization= LLMUtils.gen(apiKey,60);
            //log.info("authorization:{}",authorization);
            Request request = original.newBuilder()
                    .header(Header.AUTHORIZATION.getValue(), authorization)
                    .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        }
    }

    @Override
    public void store(List<EmbeddingResult> chunkResults) {
        String collectionName = vectorStorage.getCollectionName();
        vectorStorage.store(collectionName, chunkResults);
    }
}
