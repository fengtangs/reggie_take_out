package com.item.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.reggie.common.R;
import com.item.reggie.entity.Employee;
import com.item.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired()
    private EmployeeService employeeService;


    /**
     *
     * 员工登录逻辑.
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         *1. 首先将密码转换成md5
         *2. 根据页面提交的用户名查询数据库
         * 3.查无此人
         * 4. 有这个用户，进行密码匹配
         * 5.
         */

        //1
        String passwd= employee.getPassword();
        passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());


        //2
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp= employeeService.getOne(queryWrapper);
        //3
        if (emp==null){
            return R.error("登陆失败");
        }

        //4.密码比对，如果不一致就失败
        if(!emp.getPassword().equals(passwd)){
            return R.error("密码错误！");
        }
        //5.账号状态
        if(emp.getStatus()==0){
            return R.error("用户已被禁用");
        }
        //登录成功！
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);

    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中保存的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
