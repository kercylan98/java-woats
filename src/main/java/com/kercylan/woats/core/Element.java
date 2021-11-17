package com.kercylan.woats.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/13 2:41 下午
 * @describe 排课中的元素
 */
public class Element {
    /**
     * 班级id
     */
    protected String classId;

    /**
     * 学段值
     */
    protected int stage;

    /**
     * 级别值
     */
    protected int level;

    /**
     * 班主任id
     */
    protected String[] leaders;

    /**
     * 课程id
     */
    protected String subjectId;

    /**
     * 任教id
     */
    protected String[] teacherIds;

    /**
     * 学生id（选修班用）
     */
    protected String[] studentIds;

    /**
     * 教师id
     */
    protected String[] classrooms;

    /**
     * 课时数，大于1为连堂课
     */
    protected int classPeriod;

    /**
     * 总课时数
     */
    protected int classPeriodTotal;

    /**
     * 特定课位对应的优先级（应使用深拷贝后的引用）
     * 优先级范围为0～1，默认0.5
     */
    protected Map<CurriculumPosition, Float> priority = new HashMap<>();

    /**
     * 当前所在课位
     */
    protected CurriculumPosition curriculumPosition;

    /**
     * 班级所有课位（应使用班级课位List深拷贝后的引用）
     */
    protected List<CurriculumPosition> curriculumPositions;

    /**
     * 无效构造（声明List泛型使用）
     */
    public Element(){}

    /**
     * @param curriculumPosition 课位
     * @return 该元素对于该课位的优先级
     */
    public float getPriority(CurriculumPosition curriculumPosition) {
        if (!this.priority.containsKey(curriculumPosition)) return 0.5F;
        return this.priority.get(curriculumPosition);
    }

    /**
     * @param curriculumPosition 课位
     * @return 该元素是否禁止排在该课位
     */
    public boolean isDisable(CurriculumPosition curriculumPosition) {
        return this.getPriority(curriculumPosition) == 0F;
    }


    /**
     * @return 优先级配置总数
     */
    public int getPriorityCount() {
        return this.priority.size();
    }

    /**
     * @return 优先级高于标准的配置总数
     */
    public int getPriorityHighCount() {
        int count = 0;
        for (Map.Entry<CurriculumPosition, Float> entry: this.priority.entrySet()) {
            if (entry.getValue() > 0.5F) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return 优先级低于标准的配置总数
     */
    public int getPriorityLowCount() {
        int count = 0;
        for (Map.Entry<CurriculumPosition, Float> entry: this.priority.entrySet()) {
            if (entry.getValue() < 0.5F) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return 优先级为标准的配置总数
     */
    public int getPriorityStandardCount() {
        int count = 0;
        for (Map.Entry<CurriculumPosition, Float> entry: this.priority.entrySet()) {
            if (entry.getValue() == 0.5F) {
                count++;
            }
        }
        return count;
    }

    public String getClassId() {
        return classId;
    }

    public int getStage() {
        return stage;
    }

    public int getLevel() {
        return level;
    }

    public String[] getLeaders() {
        return leaders;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String[] getTeacherIds() {
        return teacherIds;
    }

    public String[] getStudentIds() {
        return studentIds;
    }

    public String[] getClassrooms() {
        return classrooms;
    }

    public int getClassPeriod() {
        return classPeriod;
    }

    public int getClassPeriodTotal() { return classPeriodTotal; }

    public CurriculumPosition getNowCurriculumPosition() {
        return curriculumPosition;
    }

    public List<CurriculumPosition> getClassCurriculumPositions() {
        return curriculumPositions;
    }
}
