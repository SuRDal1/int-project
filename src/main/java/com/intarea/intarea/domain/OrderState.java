package com.intarea.intarea.domain;

public enum OrderState {
    PENDING("공정대기중"),
    IN_PROGRESS("공정진행"),
    COMPLETED("공정완료");

    // 라벨 지정으로 한글 표시
    private final String label;

    OrderState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
