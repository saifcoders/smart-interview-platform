package com.mdsaifullah.smartinterview.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "🚀 Mission The Project Started Successfully!";
    }
}