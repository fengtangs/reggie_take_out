package com.item.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.common.R;
import com.item.reggie.entity.Employee;
import com.item.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


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


    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());




        //初始密码，MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());

        employee.setUpdateTime(LocalDateTime.now());

        Long empId=(Long)request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);


        employeeService.save(employee);
        return R.success("新增员工成功！");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){    //注意这里的变量名要和前端一致
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //这是一个分页构造器
        Page pageInfo = new Page(page,pageSize);

        //t条件构造器，做数据库查询用
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper<>();
        //按照姓名查询，如果用户输入查询的姓名不为空的话
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);

        //排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return  R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        Long empid=(Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empid);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee employee=employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到对应的员工信息");
    }
}
