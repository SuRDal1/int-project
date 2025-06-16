package com.intarea.intarea.domain;

public enum ProductName {
    FLOWER_POT("화분","/image/fPot.jpg"),
    VASE("꽃병","/image/vase.jpg"),
    BASKET("바구니","/image/basket.gif"),
    STORAGE_BOX("수납함","/image/sBox.jpg"),
    FRAME("액자","/image/frame.jpg"),
    LAMP_COVER("램프 커버","/image/lCover.jpg");

    // 라벨 지정으로 한글 표시
    private final String label;

    // 이미지 지정
    private final String imageUrl;

    ProductName(String label, String imageUrl) {
        this.label = label;
        this.imageUrl = imageUrl;
    }

    public String getLabel() {
        return label;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
