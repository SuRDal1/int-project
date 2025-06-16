package com.intarea.intarea.domain;

public enum Grade {

    MANAGER("관리자"),   //관리자
    INSPECTOR("검수자"), //조사관
    EMPLOYEE("직원"),
    GUEST("손님");      //손님

    private final String label;

    Grade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
