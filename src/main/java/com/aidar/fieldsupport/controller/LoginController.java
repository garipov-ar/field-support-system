// com.aidar.fieldsupport.controller.LoginController.java
package com.aidar.fieldsupport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // имя шаблона: login.html
    }
}