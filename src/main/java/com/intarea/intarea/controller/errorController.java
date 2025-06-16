package com.intarea.intarea.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping("/error")
public class errorController {

    // 400에러 웹페이지 체크
    @GetMapping("/400")
    public String error400(){

        return "error/400";
    }

    // 404에러 웹페이지 체크
    @GetMapping("/404")
    public String error404(){

        return "error/404";
    }

    // 405에러 웹페이지 체크
    @GetMapping("/405")
    public String error405(){

        return "error/405";
    }

    // 500에러 웹페이지 체크
    @GetMapping("/500")
    public String error500(){

        return "error/500";
    }

}
