package com.kercylan.woats.core;

import java.util.List;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/14 11:21 上午
 * @describe 用来操作整个课表的操纵杆
 */
public class Lever {
    private final Table table;
    private final Handwork handwork;

    /**
     * 构造操纵杆
     * @param tableConfigure 课表配置实现
     */
    public Lever(Table.TableConfigure tableConfigure) {
        this.table = new Table(tableConfigure);
        this.handwork = new Handwork(this.table);
    }

    /**
     * @return 返回手工操作实例
     */
    public Handwork getHandwork() {
        return handwork;
    }

    /**
     * 开始按照排课策略执行排课任务
     * @param adjustment 排课策略
     */
    public void Do(Adjustment adjustment) {
        while (this.table.todo.size() > 0) {
            Element next = adjustment.next(this.table.todo);
            adjustment.exec(this.table, next);
            if (this.table.todo.contains(next)) {
                this.table.unsolvable.add(next);
                this.table.todo.remove(next);
            }
            if (this.table.todo.size() == 0) {
                FinishStrategy fs = adjustment.onFinish();
                if (fs != null) {
                    List<Element> retry = fs.retry(this.table.unsolvable);
                    if (retry != null) {
                        this.table.todo.addAll(retry);
                        this.table.unsolvable.removeAll(retry);
                    }
                }
            }
        }

    }

    /**
     * 排课完成策略接口
     */
    public interface FinishStrategy {
        /**
         * @param unsolvable 无解元素
         * @return 需要再尝试的元素
         */
        List<Element> retry(List<Element> unsolvable);
    }

    /**
     * 排课策略接口
     */
    public interface Adjustment {

        /**
         * @param todos 剩余的待办元素
         * @return 下一次策略中执行的元素
         */
        Element next(List<Element> todos);

        /**
         * @param table 课表
         * @param element 元素
         */
        void exec(Table table, Element element);

        /**
         * @return 排课完成执行策略
         */
        FinishStrategy onFinish();
    }
}
