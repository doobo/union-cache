package com.github.doobo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class IndexController {
    
    @GetMapping
    public String index(){
        return UUID.randomUUID().toString();
    }
}
