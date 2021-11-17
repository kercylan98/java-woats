package com.kercylan.woats.adjustments;

import com.kercylan.woats.core.CurriculumPosition;
import com.kercylan.woats.core.Element;
import com.kercylan.woats.core.Lever;
import com.kercylan.woats.core.Table;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/14 2:47 下午
 * @describe
 */
public class KAdjustment implements Lever.Adjustment {
    private int nowTryCount = 0;
    private Table table;
    private Element element;

    public KAdjustment() {

    }

    @Override
    public Element next(List<Element> todos) {
        // 打乱
        todos.sort((o1, o2) -> new Random().nextBoolean() ? 1 : -1);
        // 尝试优先对难度较高的课程进行排课
        todos.sort((o1, o2) -> {
            boolean o2IsDifficult = o2.getStage() > o1.getStage();
            if (!o2IsDifficult && o2.getLevel() > o1.getLevel()) o2IsDifficult = true;
            if (!o2IsDifficult && o2.getPriorityLowCount() > o1.getPriorityLowCount()) o2IsDifficult = true;

            return o2IsDifficult ? 1 : -1;
        });
        return todos.get(0);
    }

    @Override
    public void exec(Table table, Element element) {
        this.table = table;
        this.element = element;

        Map<CurriculumPosition, List<Float>> cpScoreDimension = new HashMap<>();

        List<CurriculumPosition> usable = table.getUsable(element);
        if (usable.size() > 0) {
            for (CurriculumPosition cp: usable) {
                cpScoreDimension.put(cp, new ArrayList<>());

                // 评分维度
                cpScoreDimension.get(cp).add(element.getPriority(cp));
                cpScoreDimension.get(cp).add(this.calcEquilibriumDegreeScore(cp));
                cpScoreDimension.get(cp).add(this.calcContinuityScore(cp));
                cpScoreDimension.get(cp).add(this.calcTeacherWeightScore(cp));
                cpScoreDimension.get(cp).add(this.calcClassroomDistanceScore(cp));
                cpScoreDimension.get(cp).add(this.calcConflictScore(cp));
            }

            // 计算终值并找到最优课位
            CurriculumPosition topCp = null;
            float topCpScore = 0;
            for (Map.Entry<CurriculumPosition, List<Float>> entry: cpScoreDimension.entrySet()) {
                float finalValue = 0;
                for (float score: entry.getValue()) {
                    finalValue = finalValue + score;
                }
                finalValue = finalValue / entry.getValue().size();
                if (finalValue == topCpScore) {
                    boolean rb = new Random().nextBoolean();
                    topCpScore = rb ? topCpScore: finalValue;
                    topCp = rb ? topCp: entry.getKey();
                }else if (finalValue > topCpScore) {
                    topCpScore = finalValue;
                    topCp = entry.getKey();
                }
            }

            table.join(element, topCp);

            if (topCp != null) System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()) + "\ttop score: " + topCpScore + ",\tday: " + topCp.getDay() + " section: " + topCp.getSection() + " course: " + element.getSubjectId() + " \tclass: " + element.getClassId());

        }

    }

    @Override
    public Lever.FinishStrategy onFinish() {
        return unsolvable -> {
            if (this.nowTryCount < 3) {
                this.nowTryCount++;
                return unsolvable;
            }
            return null;
        };
    }

    /**
     * @param curriculumPosition 课位
     * @return 计算该课位冲突评分
     * 会造成的冲突数量打分
     */
    private float calcConflictScore(CurriculumPosition curriculumPosition) {
        return 1F;
    }

    /**
     * @param curriculumPosition 课位
     * @return 计算该课位上课教室距离评分
     * 上课切换教室的距离打分，如在A教室上完课后应该尽量前往较近的教室
     */
    private float calcClassroomDistanceScore(CurriculumPosition curriculumPosition) {
        return 1F;
    }

    /**
     * @param curriculumPosition 课位
     * @return 计算该课位教师权重评分
     * 用于计算任课教师是否工作量过多或过少
     */
    private float calcTeacherWeightScore(CurriculumPosition curriculumPosition) {
        return 1F;
    }

    /**
     * @param curriculumPosition 课位
     * @return 计算该课位连续性评分
     * 上下连续课位越高评分越低，连续量为1时比3节连堂更好，比2节连堂更差。连续课位为2时效果应为最佳。
     */
    private float calcContinuityScore(CurriculumPosition curriculumPosition) {
        float continuity = 1;
        boolean findWithUp = true;
        CurriculumPosition now = curriculumPosition.getPrevious();
        while (now != null && now.getDay() == curriculumPosition.getDay()) {
            if (this.table.isContain(now, this.element)) {
                continuity++;
            }else {
                if (!findWithUp) break;
                findWithUp = false;
                now = curriculumPosition;
            }
            now = findWithUp ? now.getPrevious() : now.getNext();
        }
        continuity = continuity == 1F ? 2.5F : continuity;
        return 1F - continuity / this.table.getYCrossed(curriculumPosition).size();
    }

    /**
     * @param curriculumPosition 课位
     * @return 计算该课位课时均衡评分
     * 课程尽量均衡分布到每一天
     */
    private float calcEquilibriumDegreeScore(CurriculumPosition curriculumPosition) {
        List<Integer> workday = table.getWorkday(element);
        float source = (float) element.getClassPeriodTotal() / (float) workday.size();
        float maxOptimumCount = (float) Math.ceil(source);
        boolean tile = true;
        for (int day: workday) {
            if (table.getCount(this.element, day) == 0) tile = false;
            if (!tile && curriculumPosition.getDay() == day) {
                return 1F;
            }
            tile = true;
        }

        for (int day: workday) {
            if (curriculumPosition.getDay() == day) return maxOptimumCount / (table.getCount(this.element, day) + 1);
        }
        return 0F;
    }
}
