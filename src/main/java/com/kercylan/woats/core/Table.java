package com.kercylan.woats.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/13 3:23 下午
 * @describe
 */
public class Table {
    protected final Map<String, Map<CurriculumPosition, List<Element>>> graph = new HashMap<>();

    /**
     * 待办元素
     */
    protected final List<Element> todo;

    /**
     * 无解元素
     */
    protected final List<Element> unsolvable = new ArrayList<>();

    /**
     * 课位与所对应班级的映射
     */
    private final Map<CurriculumPosition, String> cpcMapper = new HashMap<>();

    /**
     * @param tableConfigure 课表配置实现
     */
    protected Table(TableConfigure tableConfigure) {
        this.todo = tableConfigure.getElement();

        this.todo.forEach(element -> {
            if (!this.graph.containsKey(element.classId)) {
                this.graph.put(element.classId, new HashMap<>());
            }
            for (int i = 0; i < element.curriculumPositions.size(); i++) {
                CurriculumPosition cp = element.curriculumPositions.get(i);
                if (i > 0) {
                    element.curriculumPositions.get(i - 1).next = cp;
                    cp.previous = element.curriculumPositions.get(i - 1);
                }
                cpcMapper.put(cp, element.classId);
                this.graph.get(element.classId).put(cp, new ArrayList<>());
            }

        });

        // 不要使用foreach等，避免循环过程中数组被 join 改变
        for (int i = 0; i < this.todo.size(); i++) {
            Element element = this.todo.get(i);
            if (element.curriculumPosition != null) {
                this.join(element, element.curriculumPosition);
            }
        }

    }

    /**
     * @param a 元素a
     * @param b 元素b
     * @return 比较两个元素是否冲突
     */
    protected boolean isConflict(Element a, Element b) {
        // 元素是同一个表冲突
        if (a.equals(b)) return true;

        // 班级相同表冲突
        if (a.classId.equals(b.classId)) return true;

        // 任意一个任课教师相同表冲突
        for (String aTid: a.teacherIds) {
            for (String bTid: b.teacherIds) {
                if (aTid.equals(bTid)) return true;
            }
        }
        // 任意一个学生相同表冲突
        for (String aSid: a.studentIds) {
            for (String bSid: b.studentIds) {
                if (aSid.equals(bSid)) return true;
            }
        }
        // 任意一个教室相同表冲突
        for (String aRid: a.classrooms) {
            for (String bRid: b.classrooms) {
                if (aRid.equals(bRid)) return true;
            }
        }
        return false;
    }


    /**
     * @param curriculumPosition 课位
     * @param element 元素
     * @return 比较元素在课位中是否冲突
     */
    protected boolean isConflict(CurriculumPosition curriculumPosition, Element element) {
        // 1、遍历graph中的所有班级的课表
        // 2、在班级课表中找到课位信息匹配或者交叉的课位，遍历其包含的所有元素
        // 3、比较元素冲突，任一存在冲突则直接返回冲突
        for (Map.Entry<String, Map<CurriculumPosition, List<Element>>> sml : this.graph.entrySet()) {
            for (Map.Entry<CurriculumPosition, List<Element>> cl : sml.getValue().entrySet()) {
                CurriculumPosition cp = cl.getKey();

                if (element.isDisable(cp) || cp.isConflict(curriculumPosition)) {
                    for (Element e: cl.getValue()) {
                        if (this.isConflict(element, e)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param curriculumPosition 课位
     * @param element 元素
     * @return 该课位是否包含该元素
     */
    public boolean isContain(CurriculumPosition curriculumPosition, Element element) {
        for (Element e: this.graph.get(element.classId).get(curriculumPosition)) {
            if (e.subjectId.equals(element.subjectId)) return true;
        }
        return false;
    }

    /**
     * @param element 元素
     * @return 与该元素不冲突的课位
     */
    public List<CurriculumPosition> getUsable(Element element) {
        List<CurriculumPosition> cps = new ArrayList<>();
        for (Map.Entry<CurriculumPosition, List<Element>> cl : this.graph.get(element.classId).entrySet()) {
            if (!this.isConflict(cl.getKey(), element)) cps.add(cl.getKey());
        }
        return cps;
    }

    /**
     * @param element 元素
     * @return 与该元素冲突的课位
     */
    public List<CurriculumPosition> getUnusable(Element element) {
        List<CurriculumPosition> cps = new ArrayList<>();
        for (Map.Entry<CurriculumPosition, List<Element>> cl : this.graph.get(element.classId).entrySet()) {
            if (this.isConflict(cl.getKey(), element)) cps.add(cl.getKey());
        }
        return cps;
    }

    /**
     * @param element 元素
     * @return 获取元素对应课表工作日分别在哪几天
     */
    public List<Integer> getWorkday(Element element) {
        List<Integer> workday = new ArrayList<>();
        for(Map.Entry<CurriculumPosition, List<Element>> entry: this.graph.get(element.classId).entrySet()) {
            Integer day = entry.getKey().getDay();
            if (!workday.contains(day)) workday.add(day);
        }
        return workday;
    }

    /**
     * @param element 元素
     * @return 获取元素在某一天出现的次数
     */
    public int getCount(Element element, int day) {
        int count = 0;
        for(Map.Entry<CurriculumPosition, List<Element>> entry: this.graph.get(element.classId).entrySet()) {
            if (entry.getKey().getDay() == day) {
                for (Element e: entry.getValue()) {
                    if (e.subjectId.equals(element.subjectId)) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    /**
     * @param curriculumPosition 课位
     * @return 与该课位产生非天交叉的其他课位
     */
    public List<CurriculumPosition> getXCrossed(CurriculumPosition curriculumPosition) {
        List<CurriculumPosition> cs = new ArrayList<>();
        for (Map.Entry<CurriculumPosition, List<Element>> entry: this.graph.get(this.cpcMapper.get(curriculumPosition)).entrySet()) {
            CurriculumPosition cp = entry.getKey();
            if (cp.isTime()) {
                if ((curriculumPosition.getStartTime() < cp.getStartTime() && curriculumPosition.getEndTime() > cp.getStartTime()) ||
                        (curriculumPosition.getStartTime() > cp.getStartTime() && curriculumPosition.getStartTime() < cp.getEndTime()) ||
                        (curriculumPosition.equals(cp)) && !cp.equals(curriculumPosition)) cs.add(cp);
            }else {
                if (cp.getSection() == curriculumPosition.getSection() && !cp.equals(curriculumPosition)) cs.add(cp);
            }
        }
        return cs;
    }

    /**
     * @param curriculumPosition 课位
     * @return 与该课位同一天的其他课位
     */
    public List<CurriculumPosition> getYCrossed(CurriculumPosition curriculumPosition) {
        List<CurriculumPosition> cs = new ArrayList<>();
        for (Map.Entry<CurriculumPosition, List<Element>> entry: this.graph.get(this.cpcMapper.get(curriculumPosition)).entrySet()) {
            CurriculumPosition cp = entry.getKey();
            if (cp.getDay() == curriculumPosition.getDay() && !cp.equals(curriculumPosition)) cs.add(cp);
        }
        return cs;
    }

    /**
     * @param element 要加入特定课位的元素
     * @param curriculumPosition 课位
     */
    public void join(Element element, CurriculumPosition curriculumPosition) {
        if (element.curriculumPosition != null) {
            this.graph.get(element.classId).get(element.curriculumPosition).remove(element);
        }
        this.graph.get(element.classId).get(curriculumPosition).add(element);
        element.curriculumPosition = curriculumPosition;
        this.todo.remove(element);
    }

    /**
     * @param element 放回代办中的元素
     */
    public void leave(Element element) {
        if (element.curriculumPosition != null) {
            this.graph.get(element.classId).get(element.curriculumPosition).remove(element);
        }
        this.todo.add(element);
    }

    /**
     * 课表配置接口描述
     */
    public interface TableConfigure {
        /**
         * @return 获取参与排课的元素
         */
        List<Element> getElement();
    }

}
