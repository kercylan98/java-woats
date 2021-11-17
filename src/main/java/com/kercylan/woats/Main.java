package com.kercylan.woats;

import com.kercylan.woats.adjustments.KAdjustment;
import com.kercylan.woats.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author kercylan98
 * @version 1.0
 * @date 2021/10/13 3:57 下午
 * @describe
 */
public class Main {

    public static void main(String[] args) {
        List<CurriculumPosition> cps = new ArrayList<>();
        cps.add(new CurriculumPosition(1, 1));
        cps.add(new CurriculumPosition(1, 2));
        cps.add(new CurriculumPosition(1, 3));
        cps.add(new CurriculumPosition(1, 4));
        cps.add(new CurriculumPosition(1, 5));
        cps.add(new CurriculumPosition(1, 6));
        cps.add(new CurriculumPosition(1, 7));

        cps.add(new CurriculumPosition(2, 1));
        cps.add(new CurriculumPosition(2, 2));
        cps.add(new CurriculumPosition(2, 3));
        cps.add(new CurriculumPosition(2, 4));
        cps.add(new CurriculumPosition(2, 5));
        cps.add(new CurriculumPosition(2, 6));
        cps.add(new CurriculumPosition(2, 7));

        cps.add(new CurriculumPosition(3, 1));
        cps.add(new CurriculumPosition(3, 2));
        cps.add(new CurriculumPosition(3, 3));
        cps.add(new CurriculumPosition(3, 4));
        cps.add(new CurriculumPosition(3, 5));
        cps.add(new CurriculumPosition(3, 6));
        cps.add(new CurriculumPosition(3, 7));

        cps.add(new CurriculumPosition(4, 1));
        cps.add(new CurriculumPosition(4, 2));
        cps.add(new CurriculumPosition(4, 3));
        cps.add(new CurriculumPosition(4, 4));
        cps.add(new CurriculumPosition(4, 5));
        cps.add(new CurriculumPosition(4, 6));
        cps.add(new CurriculumPosition(4, 7));

        cps.add(new CurriculumPosition(5, 1));
        cps.add(new CurriculumPosition(5, 2));
        cps.add(new CurriculumPosition(5, 3));
        cps.add(new CurriculumPosition(5, 4));
        cps.add(new CurriculumPosition(5, 5));
        cps.add(new CurriculumPosition(5, 6));
        cps.add(new CurriculumPosition(5, 7));

        Lever lever = new Lever(() -> {
            List<Element> es = new ArrayList<>();
            es.addAll(ClassBuilder.ordinaryClass("1班", 1, 1, cps).
                    addSubject("语文", "张三").
                    setClassPeriod(1, 1, 1, 1, 1, 1, 1).
                    ok().

                    addSubject("数学", "李四").
                    setClassPeriod(1, 1, 1, 1).
                    ok().

                    addSubject("英语", "王五").
                    setClassPeriod(1, 1, 1, 1, 1).
                    ok().

                    submit().toElements()
            );
            es.addAll(ClassBuilder.electiveClass("足球", new String[]{"刘红"}, new String[]{"小明"}, cps).
                    setClassPeriod(1, 1).ok().toElements());

            return es;
        });


        lever.Do(new KAdjustment());
    }


}
