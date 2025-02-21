package com.swyp.goal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 뷰를 제공하는 컨트롤러1
 */
@Controller
@RequestMapping("api/v1/home")
public class HomeController {

    @GetMapping
    public String home() {
        return "home"; // home.jsp를 반환11111111
    }

} 