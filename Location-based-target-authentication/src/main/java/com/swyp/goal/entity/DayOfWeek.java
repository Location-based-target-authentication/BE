package com.swyp.goal.entity;

public enum DayOfWeek {
    MON, TUE, WED, THU, FRI, SAT, SUN;
    //(포인트)
    public static DayOfWeek fromJavaTime(java.time.DayOfWeek javaDay) {
        return DayOfWeek.valueOf(javaDay.name().substring(0, 3).toUpperCase()); // 앞의 3글자 추출
    }
}