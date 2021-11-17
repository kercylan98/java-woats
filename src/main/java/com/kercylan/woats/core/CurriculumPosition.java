package com.kercylan.woats.core;

import java.io.*;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/13 1:02 下午
 * @describe 基于时间的课程定位实现
 *
 * 满足每个课位都拥有独自的时间分配
 */
public class CurriculumPosition implements Serializable {
    /**
     * 哪一天
     */
    private final int day;

    /**
     * 第几节
     */
    private int section;

    /**
     * 开始于某小时
     */
    private int startHour;

    /**
     * 开始于某分钟
     */
    private int startMinute;

    /**
     * 结束于某小时
     */
    private int endHour;

    /**
     * 结束于某分钟
     */
    private int endMinute;

    /**
     * 小数格式的开始时间
     */
    private double startTime;

    /**
     * 小数格式的结束时间
     */
    private double endTime;

    /**
     * 是否为一个时间课位
     */
    private final boolean isTime;

    /**
     * 上一个课位
     */
    protected CurriculumPosition previous;

    /**
     * 下一个课位
     */
    protected CurriculumPosition next;

    /**
     * 是否是一天中的第一节课
     */
    protected boolean isStart = false;

    /**
     * 是否是一天中的最后一节课
     */
    protected boolean isEnd = false;

    /**
     * @return 是否为阶段性最后一节。中午最后一节或者晚上最后一节
     */
    public boolean isStageEnd() {
        return isStageEnd;
    }

    /**
     * @return 是否为阶段性第一节。中午第一节或者晚上第一节
     */
    public boolean isStageStart() {
        if (this.previous != null && this.previous.isStart) return false;
        return this.previous == null || this.isStart || this.previous.isStageEnd;
    }

    /**
     * @param stageEnd 是否为阶段性最后一节。中午最后一节或者晚上最后一节
     * @return 调整后的该课位
     */
    public CurriculumPosition setStageEnd(boolean stageEnd) {
        isStageEnd = stageEnd;
        return this;
    }

    /**
     * 是否是一个时间段的最后一节，如上午最后一节
     */
    protected boolean isStageEnd = false;

    /**
     * 常规课位构造
     * @param day 哪一天
     * @param section 第几节
     */
    public CurriculumPosition(int day, int section) {
        this.day = day;
        this.section = section;
        this.isTime = false;
    }

    /**
     * 时间课位构造
     * @param day 哪一天
     * @param startHour 开始于哪个小时
     * @param startMinute 开始于多少分钟
     * @param endHour 结束于哪个小时
     * @param endMinute 结束于多少分钟
     */
    public CurriculumPosition(int day, int startHour, int startMinute, int endHour, int endMinute) {
        this.day = day;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.startTime = CurriculumPosition.toDoubleTime(this.startHour, this.startMinute);
        this.endTime = CurriculumPosition.toDoubleTime(this.endHour, this.endMinute);
        this.isTime = true;
    }

    /**
     * @param curriculumPosition 比较课位
     * @return 与比较课位是否存在冲突
     */
    public boolean isConflict(CurriculumPosition curriculumPosition) {
        if (this.isTime) {
            if (this.day != curriculumPosition.day) return false;
            return (this.startTime < curriculumPosition.startTime && this.endTime > curriculumPosition.startTime) ||
                    (this.startTime > curriculumPosition.startTime && this.startTime < curriculumPosition.endTime) ||
                    (this.equals(curriculumPosition));
        }
        return (curriculumPosition.day == this.day && curriculumPosition.section == this.section ||
                curriculumPosition.equals(this));
    }

    /**
     * @param hour 小时
     * @param minute 分钟
     * @return 返回一个浮点型的天内时间
     */
    private static double toDoubleTime(int hour, int minute) {
        if (hour > 23 || hour < 0 || minute < 0 || minute > 59) {
            return 0;
        }
        if (minute == 0) {
            return hour;
        }
        return hour + (minute / 60.0);
    }

    /**
     * @return 深拷贝后的课位
     */
    protected CurriculumPosition deepClone() {
        CurriculumPosition copy = null;
        try {
            ByteArrayOutputStream aos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(aos);
            oos.writeObject(this);
            ByteArrayInputStream ais = new ByteArrayInputStream(aos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(ais);
            copy = (CurriculumPosition) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return copy;
    }

    public int getDay() {
        return day;
    }

    public int getSection() {
        return section;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public boolean isTime() {
        return isTime;
    }

    public CurriculumPosition getPrevious() {
        return previous;
    }

    public CurriculumPosition getNext() {
        return next;
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isEnd() {
        return isEnd;
    }

}
