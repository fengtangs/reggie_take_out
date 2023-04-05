package com.item.reggie.controller;

import com.item.reggie.common.R;
import com.item.reggie.entity.SMSCode;
import com.item.reggie.entity.User;
import com.item.reggie.service.SMSCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Time;

@RestController
@Slf4j
@RequestMapping("/usr")
public class SMSController {

    @Autowired
    private SMSCodeService service;

    @PostMapping("/code")
    public R<String> code (HttpServletRequest request, @RequestBody SMSCode smsCode){



        smsCode.setCode(service.generatorcode(smsCode.getPhone()));
        smsCode.setTime(System.currentTimeMillis());
        log.info(String.valueOf(smsCode));
        return R.success("验证码发送成功");


    }
}
