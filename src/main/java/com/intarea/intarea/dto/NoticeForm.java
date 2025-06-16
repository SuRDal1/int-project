package com.intarea.intarea.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeForm {

    private Long id;

    @NotBlank(message = "제목 입력은 필수입니다")
    private String title;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String content;

}
