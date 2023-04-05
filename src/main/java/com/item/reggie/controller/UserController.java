package com.item.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.reggie.common.BaseContext;
import com.item.reggie.common.R;
import com.item.reggie.entity.SMSCode;
import com.item.reggie.entity.User;
import com.item.reggie.service.SMSCodeService;
import com.item.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SMSCodeService smsCodeService;

    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody SMSCode smsCode){
        log.info("info={}",smsCode);
        String tele=smsCode.getPhone();

        if(!smsCodeService.checkcode(smsCode)){
            return R.error("验证码错误");
        }

        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,tele);
        User emp= userService.getOne(queryWrapper);
        if(emp==null){
            User user=new User();
                user.setName(tele);
                user.setPhone(tele);
                user.setStatus(1);
                userService.save(user);
            LambdaQueryWrapper<User> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(User::getPhone,tele);
            emp= userService.getOne(queryWrapper);
        }
        //登录成功！
        request.getSession().setAttribute("user",emp.getId());
        return R.success(emp);




    }


    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        BaseContext.setCurrentId(null);
        request.getSession().setAttribute("user",null);
        return R.success("成功");


    }

}
