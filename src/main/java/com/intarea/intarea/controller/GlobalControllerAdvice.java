package com.intarea.intarea.controller;

import com.intarea.intarea.domain.Users;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice  //모든 페이지에 로그인 유지 기능 추가
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final HttpSession session;

    @ModelAttribute
    public void addLoginMemberToModel(Model model) {
        Users loginMember = (Users) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("loginMember", loginMember);

    }
}
