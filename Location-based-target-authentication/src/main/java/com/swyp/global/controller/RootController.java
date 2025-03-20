package com.swyp.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RootController {
    
    @GetMapping("/")
    @ResponseBody
    public String root() {
        return "Willgo API Server is running!";
    }
} 