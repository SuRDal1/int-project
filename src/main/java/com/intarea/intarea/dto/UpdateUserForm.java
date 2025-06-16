package com.intarea.intarea.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserForm {

    private Long id;

    @NotEmpty(message = "회원이름은 필수입니다")
    private String name;


    private String loginId;

    @NotEmpty(message = "비밀번호는 필수입니다.")
    private String password;

    @NotEmpty(message = "직급 선택은 필수입니다.")
    private String grade;
    
    @NotEmpty(message = "이메일은 필수입니다.")
    private String email;

    @NotEmpty(message = "전화번호는 필수입니다.")
    private String phone;
}
