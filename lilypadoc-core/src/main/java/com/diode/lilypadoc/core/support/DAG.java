package com.diode.lilypadoc.core.support;

import java.util.*;

public class DAG<T> {
    private List<Edge<T>> edgeList;

    public DAG() {
        this.edgeList = new ArrayList<>();
    }

    public void addEdge(Edge<T> edge) {
        edgeList.add(edge);
    }

    /**
     * TODO 前置要求插件不循环依赖
     * 拓扑排序
     */
    public List<T> sort() {
        if (Objects.isNull(edgeList) || edgeList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> res = new ArrayList<>();
        Map<T, Integer> inDegreeMap = new HashMap<>();
        // get the in-degree for each course
        for (Edge<T> edge : edgeList) {
            inDegreeMap.putIfAbsent(edge.from(), 0);
            inDegreeMap.compute(edge.to(), (k, v) -> Objects.isNull(v) ? 1 : ++v);
        }
        // put courses with indegree == 0 to queue
        Queue<T> queue = new LinkedList<>();
        inDegreeMap.forEach((k, v) -> {
            if (v == 0) {
                queue.offer(k);
            }
        });

        // execute the course
        int i = 0;
        while (!(queue.isEmpty())) {
            T curr = queue.poll();
            res.add(curr);
            i++;

            // remove the pre - curr
            for (Edge<T> edge : edgeList) {
                if (Objects.equals(edge.from(), curr)) {
                    inDegreeMap.computeIfPresent(edge.to(), (k, v) -> --v);
                    if (inDegreeMap.get(edge.to()) == 0) {
                        queue.offer(edge.to());
                    }
                }
            }
        }
        return res;
    }


    public static class Edge<T> {
        private final T fromNode;
        private final T toNode;

        public Edge(T fromNode, T toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        public T from() {
            return fromNode;
        }

        public T to() {
            return toNode;
        }
    }
}
