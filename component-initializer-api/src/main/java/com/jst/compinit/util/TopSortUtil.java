package com.jst.compinit.util;

import com.jst.compinit.IComponentInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 拓扑排序的工具类
 */
public class TopSortUtil {
    public static List<IComponentInfo> topSort(List<IComponentInfo> componentInfoList){
        //将图初始化出来
        List<Vertex> vertexList = new ArrayList<>();
        for (IComponentInfo componentInfo : componentInfoList) {
            Vertex vertex = new Vertex();
            vertex.componentInfo = componentInfo;
            vertexList.add(vertex);
        }
            //初始化邻接表
        for (Vertex vertex : vertexList) {
            String[] dependencies = vertex.componentInfo.getDependencies();
            for (String dependency : dependencies) {
                for (Vertex vertex1 : vertexList) {
                    if (dependency.equals(vertex1.componentInfo.getName())) {
                        vertex1.adjList.add(vertex);
                        vertex.indegree++;
                    }
                }
            }
        }

        List<IComponentInfo> sortedList = new ArrayList<>();
        int counter = 0;

        /*
        拓扑排序大致步骤：
        1.初始化一个入度为零顶点的队列
        2.出队一个顶点  （该顶点为拓扑排序的下一个顶点，可以将该顶点保存在list里面）
        3.删除该顶点及该顶点的边
        （这里说的删除顶点及边是直观上理解，对应到的代码逻辑就是，
        删除顶点：实际上在上一步出队时已经做过删除了；
        删除边：将该顶点的邻接表里的顶点入度-1）
        4.将入度-1后入度等于0的顶点添加到队列中
        5.重复 2-4 步骤

        循环退出条件：队列为空

        有环判断条件：队列里出队的顶点的计数 小于 顶点的总数
        */


        //初始化一个入度为零顶点的队列
        Queue<Vertex> queue = new LinkedList<>();
        for (Vertex vertex : vertexList) {
            if (vertex.indegree == 0) {
                queue.add(vertex);
            }
        }

        while (!queue.isEmpty()) {
            Vertex vertex = queue.remove();
            counter++;
            sortedList.add(vertex.componentInfo);
            for (Vertex vertex1 : vertex.adjList) {
                if (--vertex1.indegree == 0) {
                    queue.add(vertex1);
                }
            }
        }

        //判断是否有环
        if (counter < vertexList.size()){
            //构成环的List
            List<IComponentInfo> hasLoopList = new ArrayList<>();

            for (IComponentInfo componentInfo : componentInfoList) {
                if (!sortedList.contains(componentInfo)) {
                    hasLoopList.add(componentInfo);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("有一个dependency循环依赖存在于下列component中:");
            for (IComponentInfo componentInfo : hasLoopList) {
                sb.append(componentInfo.getComponent().getClass().getName())
                        .append(",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            throw new IllegalArgumentException(sb.toString());
        }

        return sortedList;
    }
}
