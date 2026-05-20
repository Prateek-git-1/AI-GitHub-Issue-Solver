package com.prateek.ai.aigithubissuesolver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/hello")
    public String helo() {
        System.out.println("hello");
        return "Hello Guys";
    }
}
