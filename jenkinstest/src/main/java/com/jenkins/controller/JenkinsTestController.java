package com.jenkins.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mengximao
 * @since 1.0.0
 */
@RestController
public class JenkinsTestController {

    @GetMapping("test")
    public String test(){
        return "hello jenkins !";
    }
}
