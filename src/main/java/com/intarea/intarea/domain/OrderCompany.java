package com.intarea.intarea.domain;


public enum OrderCompany {
    ACOM("A사"),
    BCOM("B사"),
    CCOM("C사"),
    DCOM("D사"),
    ECOM("E사");

    // 라벨 지정으로 한글 표시
    private final String label;

    OrderCompany(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
