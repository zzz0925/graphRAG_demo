package com.store.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class LabelPropagation {
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> edges;
    private int iteration;
    private int milliseconds = 300;
    private final int NOT_UPDATED = -1;
    private int maxIteration = 2000;
    public void init(List<Map<String, Object>> nodes, List<Map<String, Object>> edges){
        this.nodes = nodes;
        this.edges = edges;
        initializeLabels();
        iteration = 1;
    }
    private void initializeLabels(){
        int x = 0;
        for (Map<String, Object> node : nodes) {
            node.put("startLabel",x++);
            node.put("updatedLabel",NOT_UPDATED);
        }
    }

    public void compute(){
        int count;
        int numberofnodesingraph = nodes.size();
        do{
            count = 0;
            Stack<Map<String, Object>> stack = new Stack<>();
            for (Map<String, Object> node : nodes) {
                stack.add(node);
            }
            Collections.shuffle(stack);

            while(!stack.isEmpty()){
                Map<String, Object> actualNode = stack.pop();

                int maxLabel = getLabelThatMaximumNumberOfNeighbourHave(actualNode);
                actualNode.put("updatedLabel",maxLabel);
            }

            for (Map<String, Object> node : nodes) {
                int updatedLabel = (int) node.get("updatedLabel");
                node.put("updatedLabel",NOT_UPDATED);
                node.put("startLabel",updatedLabel);
            }

            for (Map<String, Object> node : nodes) {
                log.info("node: " + node.get("id") + " startLabel: " + node.get("startLabel"));
            }
            System.out.println("Iteration: " + iteration);
            iteration++;
        }while(iteration < maxIteration);
    }

    public Set<Map<String,Object>> getNeighbourList(Map<String, Object> node){
        Set<Map<String, Object>> neighbourList  = new HashSet<>();
        for (Map<String, Object> edge : edges) {
            if(edge.get("outV").equals(node.get("id"))){
                for (Map<String, Object> n : nodes) {
                    if(n.get("id").equals(edge.get("inV"))){
                        neighbourList.add(n);
                    }
                }
            }
            if(edge.get("inV").equals(node.get("id"))){
                for (Map<String, Object> n : nodes) {
                    if(n.get("id").equals(edge.get("outV"))){
                        neighbourList.add(n);
                    }
                }
            }
        }
        return neighbourList;
    }

    public ArrayList<Integer> getNeighbourLabelList(Set<Map<String, Object>> neighbourList,UpdateMode updateMode){
        ArrayList<Integer> neighbourLabelList = new ArrayList<>();
        for (Map<String, Object> neighbour : neighbourList) {
            int updatedLabel = (int) neighbour.get("updatedLabel");
            if(updateMode == UpdateMode.ASYNCHRONOUS_MODE){
                if (updatedLabel == NOT_UPDATED) {
                    neighbourLabelList.add((Integer) neighbour.get("startLabel"));
                } else {
                    neighbourLabelList.add(updatedLabel);
                }
            } else if(updateMode == UpdateMode.SYNCHRONOUS_MODE) {
                neighbourLabelList.add((Integer) neighbour.get("startLabel"));
            }
        }
        return neighbourLabelList;
    }

    public ArrayList<Integer> createListWithMaxmumLabels(ArrayList<Integer> neighbourLabelList) {
        HashMap<Integer, Integer> map = new HashMap<>();
        int max = 0;
        for (int i = 0; i < neighbourLabelList.size(); i++) {
            if (map.containsKey(neighbourLabelList.get(i))) {
                map.put(neighbourLabelList.get(i), map.get(neighbourLabelList.get(i)) + 1);
            } else {
                map.put(neighbourLabelList.get(i), 1);
            }
            if (map.get(neighbourLabelList.get(i)) > max){
                max = map.get(neighbourLabelList.get(i));
            }
        }

        ArrayList<Integer> maxLabels = new ArrayList<>();
        for (int key : map.keySet()) {
            if (map.get(key) == max) {
                maxLabels.add(key);
            }
        }
        log.info("maxLabels: " + maxLabels);
        return maxLabels;
    }

    public int getLabelThatMaximumNumberOfNeighbourHave(Map<String, Object> node) {
        Set<Map<String, Object>> neighbourList = getNeighbourList(node);
        ArrayList<Integer> neighbourLabelList = getNeighbourLabelList(neighbourList, UpdateMode.SYNCHRONOUS_MODE);
        ArrayList<Integer> listWithMaxmumLabels = createListWithMaxmumLabels(neighbourLabelList);

        // 获取当前节点的原始标签
        Integer currentNodeStartLabel = (Integer) node.get("startLabel");

        // 如果当前节点的原始标签在最大频率标签列表中，优先选择它
        if (listWithMaxmumLabels.contains(currentNodeStartLabel)) {
            return currentNodeStartLabel;
        }

        // 否则，随机选择一个标签（如果需要的话）
        if (!listWithMaxmumLabels.isEmpty()) {
            Collections.shuffle(listWithMaxmumLabels); // 打乱列表顺序以随机选择
            return listWithMaxmumLabels.get(0);
        } else {
            return (int) node.get("startLabel"); // 返回自身标签
        }
    }
}
