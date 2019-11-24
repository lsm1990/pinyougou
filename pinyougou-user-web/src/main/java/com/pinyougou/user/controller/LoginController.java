package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  lsm
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map showName(){
      String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map=new HashMap(16);
        map.put("loginName",loginName);
        return map;
    }


}
