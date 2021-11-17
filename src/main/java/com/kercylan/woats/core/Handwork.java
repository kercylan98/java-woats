package com.kercylan.woats.core;

import java.util.List;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/11/15 2:14 下午
 * @describe
 */
public class Handwork {
    private Table table;

    private Handwork() {};
    protected Handwork(Table table) {
        this.table = table;
    };


    /**
     * @return 待排课的元素
     */
    public List<Element> getWaitJoins() {
        return this.table.todo;
    }

}
