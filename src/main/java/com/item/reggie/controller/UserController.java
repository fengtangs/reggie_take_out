package com.item.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.reggie.common.BaseContext;
import com.item.reggie.common.R;
import com.item.reggie.entity.User;
import com.item.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody User user){

        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,user.getPhone());
            User emp= userService.getOne(queryWrapper);
            log.info("info={}",user);
            if(emp==null){
                return R.error("登陆失败");
            }

            else
            {

                //登录成功！
                request.getSession().setAttribute("user",emp.getId());
                return R.success(emp);
            }



    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        BaseContext.setCurrentId(null);
        request.getSession().setAttribute("user",null);
        return R.success("成功");


    }

}
