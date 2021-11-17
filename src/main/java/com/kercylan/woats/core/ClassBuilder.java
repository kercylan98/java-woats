package com.kercylan.woats.core;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/13 11:40 上午
 * @describe 基础班级数据建造器
 */
public class ClassBuilder {

    /**
     * 班级id（含行政选修班）
     */
    private String classId;

    /**
     * 班级所处学段号码，数值越大表示学段越高
     */
    private int stage = Integer.MIN_VALUE;

    /**
     * 班级所处年级号码，数值越大表示年级越高
     */
    private int level = Integer.MIN_VALUE;

    /**
     * 班主任集合
     */
    private String[] leaders = {};

    /**
     * 学生集合
     */
    private String[] studentIds = {};

    /**
     * 班级包含的课程集合
     */
    private final List<Subject> subjects = new ArrayList<>();

    /**
     * 班级上课课位
     */
    private final List<CurriculumPosition> curriculumPositions = new ArrayList<>();


    /**
     * 课程信息内部类
     */
    private static class Subject {

        /**
         * 课程id，如果是选修班的情况下，通常与classId相同
         */
        private String subjectId;

        /**
         * 任课教师集合
         */
        private String[] teacherIds = {};

        /**
         * 上课教室集合
         */
        private String[] classrooms = {};

        /**
         * 课时数 {1, 1, 2} >> 表总共4节，其中2节为连堂课
         */
        private int[] classPeriod = {};

        /**
         * 固定课位
         */
        private CurriculumPosition[] fixed = {};

        /**
         * 课位优先级
         */
        private final Map<CurriculumPosition, Float> priority = new HashMap<>();
    }

    private static void initCPS(ClassBuilder builder, List<CurriculumPosition> cps) {
        int day = Integer.MIN_VALUE;
        for (CurriculumPosition cp : cps) {
            if (day != cp.getDay()) {
                cp.isStart = true;
                System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()) + "\tset curriculum position is " + cp.getDay() + " day start. section: " + cp.getSection());
                if (builder.curriculumPositions.size() > 0) {
                    builder.curriculumPositions.get(builder.curriculumPositions.size() - 1).isEnd = true;
                    System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()) + "\tset curriculum position is " + builder.curriculumPositions.get(builder.curriculumPositions.size() - 1).getDay() + " day end.   section: " + builder.curriculumPositions.get(builder.curriculumPositions.size() - 1).getSection());
                }
                day = cp.getDay();
            }
            builder.curriculumPositions.add(cp.deepClone());
        }
    }

    /**
     * @param classId 待创建班级id
     * @param stage 班级所属学段
     * @param level 班级所属年级
     * @param curriculumPositions 课时定位信息（数组顺序决定课位顺序）
     * @return 返回行政班构建选项
     */
    public static BuildOrdinaryClassOptions ordinaryClass(String classId, int stage, int level, List<CurriculumPosition> curriculumPositions) {
        ClassBuilder builder = new ClassBuilder();
        builder.classId = classId;
        builder.stage = stage;
        builder.level = level;
        ClassBuilder.initCPS(builder, curriculumPositions);
        return new BuildOrdinaryClassOptions() {
            @Override
            public BuildOrdinaryClassOptions setLeaders(String... leaderIds) {
                builder.leaders = leaderIds;
                return this;
            }

            @Override
            public SubjectOptions<BuildOrdinaryClassOptions> addSubject(String subjectId, String... teacherIds) {
                ClassBuilder.Subject subject = new ClassBuilder.Subject();
                subject.subjectId = subjectId;
                subject.teacherIds = teacherIds;
                return new SubjectOptions<>(builder, subject, this);
            }

            @Override
            public ClassBuilder submit() {
                return builder;
            }
        };
    }

    /**
     * @param classId 待创建班级id
     * @param teacherIds 班级任课教师
     * @param curriculumPositions 课时定位信息（数组顺序决定课位顺序）
     * @return 选修班课程构建选项
     */
    public static SubjectOptions<ClassBuilder> electiveClass(String classId, String[] teacherIds, String[] studentIds, List<CurriculumPosition> curriculumPositions) {
        ClassBuilder builder = new ClassBuilder();
        builder.classId = classId;
        builder.leaders = teacherIds;
        builder.studentIds = studentIds;
        ClassBuilder.initCPS(builder, curriculumPositions);

        ClassBuilder.Subject subject = new ClassBuilder.Subject();
        subject.subjectId = classId;
        subject.teacherIds = teacherIds;
        return new SubjectOptions<>(builder, subject, builder);
    }


    /**
     * 行政班构建选项
     */
    public interface BuildOrdinaryClassOptions {
        /**
         * @param leaderIds 设置班主任
         * @return 行政班构建选项
         */
        BuildOrdinaryClassOptions setLeaders(String ...leaderIds);

        /**
         * @param subjectId 课程id
         * @param teacherIds 任课教师
         * @return 课程选项
         */
        SubjectOptions<BuildOrdinaryClassOptions> addSubject(String subjectId, String ...teacherIds);

        /**
         * @return 返回建造器
         */
        ClassBuilder submit();
    }


    /**
     * @param <T> 课程选项
     */
    public static class SubjectOptions<T> {

        /**
         * 创建器
         */
        private final ClassBuilder builder;

        /**
         * 课程生成器实例
         */
        private final ClassBuilder.Subject subject;

        /**
         * 用于阻断行政班或者选修班流程
         */
        private final T t;


        /**
         * @param subject 课程生成器实例
         * @param t 提交时候的返回类型
         */
        private SubjectOptions(ClassBuilder builder, ClassBuilder.Subject subject, T t) {
            this.builder = builder;
            this.subject = subject;
            this.t = t;
        }

        /**
         * @param classPeriod 课时安排
         * @return 课程选项
         */
        public SubjectOptions<T> setClassPeriod(int ...classPeriod) {
            this.subject.classPeriod = classPeriod;
            return this;
        }

        /**
         * @param cpc 需要设置固定课的课位
         * @return 课程选项
         */
        public SubjectOptions<T> setFixed(CurriculumPositionChoicer cpc) {
            this.subject.fixed = cpc.choiceCurriculumPosition(this.builder.curriculumPositions);
            return this;
        }


        /**
         * @param cpc 需要设置禁排课的课位
         * @return 课程选项
         */
        public SubjectOptions<T> setDisables(CurriculumPositionChoicer cpc) {
            for (CurriculumPosition cp: cpc.choiceCurriculumPosition(this.builder.curriculumPositions)) {
                this.subject.priority.put(cp, 0F);
            }
            return this;
        }

        /**
         * @param priority 优先级
         * @param cpc 需要设置优先级的课位
         * @return 课程选项
         */
        public SubjectOptions<T> setPriority(float priority, CurriculumPositionChoicer cpc) {
            for (CurriculumPosition cp: cpc.choiceCurriculumPosition(this.builder.curriculumPositions)) {
                if (priority < 0F) {
                    this.subject.priority.put(cp, 0F);
                }else if (priority > 1F){
                    this.subject.priority.put(cp, 1F);
                }else {
                    this.subject.priority.put(cp, priority);
                }
            }
            return this;
        }

        /**
         * @param classrooms 上课教室
         * @return 课程选项
         */
        public SubjectOptions<T> setClassrooms(String ...classrooms) {
            this.subject.classrooms = classrooms;
            return this;
        }

        /**
         * @return 返回行政班构建选项或已完成的选修班生成器
         */
        public T ok() {
            this.builder.subjects.add(this.subject);
            return this.t;
        }
    }


    public List<Element> toElements() {
        List<Element> es = new ArrayList<>();
        for (Subject sub : this.subjects) {

            // 生成普通课程
            for (int n : sub.classPeriod) {
                es.add(this.createElement(sub, n, null));
            }

            // 生成固定课程
            for (CurriculumPosition curriculumPosition : sub.fixed) {
                es.add(this.createElement(sub, 1, curriculumPosition));
            }

        }
        return es;
    }

    private Element createElement(Subject sub, int classPeriod, CurriculumPosition curriculumPosition) {
        int classPeriodTotal = 0;
        for (int period : sub.classPeriod) {
            classPeriodTotal = classPeriodTotal + period;
        }

        Element element = new Element();

        element.classId = this.classId;
        element.stage = this.stage;
        element.level = this.level;
        element.leaders = this.leaders;
        element.studentIds = this.studentIds;
        element.subjectId = sub.subjectId;
        element.teacherIds = sub.teacherIds;
        element.classPeriod = classPeriod;
        element.classPeriodTotal = classPeriodTotal;
        element.priority = sub.priority;
        element.curriculumPosition = curriculumPosition;
        element.classrooms = sub.classrooms;
        element.curriculumPositions = this.curriculumPositions;


        return element;
    }

    /**
     * 用于选择对应拷贝后课位的选择器，应该避免直接传入
     */
    public interface CurriculumPositionChoicer {
        CurriculumPosition[] choiceCurriculumPosition(List<CurriculumPosition> cps);
    }
}
