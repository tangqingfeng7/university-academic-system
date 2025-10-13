package com.university.ems.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时间槽模型
 * 表示一周中的某个时间段
 * 
 * @author Academic System Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    /**
     * 星期几（1-7：周一到周日）
     */
    private Integer dayOfWeek;

    /**
     * 时段（1-8：第1节到第8节）
     */
    private Integer slot;

    /**
     * 创建时间槽
     */
    public static TimeSlot of(Integer dayOfWeek, Integer slot) {
        return new TimeSlot(dayOfWeek, slot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return dayOfWeek.equals(timeSlot.dayOfWeek) && slot.equals(timeSlot.slot);
    }

    @Override
    public int hashCode() {
        return 31 * dayOfWeek + slot;
    }

    @Override
    public String toString() {
        return "周" + getDayDescription() + "第" + slot + "节";
    }

    private String getDayDescription() {
        String[] days = {"", "一", "二", "三", "四", "五", "六", "日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : String.valueOf(dayOfWeek);
    }
}

