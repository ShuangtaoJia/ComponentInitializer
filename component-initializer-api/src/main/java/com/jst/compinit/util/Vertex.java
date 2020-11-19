package com.jst.compinit.util;

import com.jst.compinit.IComponentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 顶点
 */
class Vertex {
    public IComponentInfo componentInfo;
    /**
     * 邻接表
     *
     * 这里给ArrayList的初始容量设置为2，是因为ArrayList的初始容量默认为10，而一般情况下Component设置dependency的情况比较少，
     * 所以邻接表的数据量一般不会很多，这里设置为2可以节省空间
     */
    public List<Vertex> adjList = new ArrayList<>(2);

    /**
     * 入度
     */
    public int indegree = 0;

}
