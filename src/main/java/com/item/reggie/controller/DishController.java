package com.item.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.common.R;
import com.item.reggie.dto.DishDto;
import com.item.reggie.entity.Dish;
import com.item.reggie.entity.Employee;
import com.item.reggie.service.DishFlavorService;
import com.item.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜品管理
 *
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功保存");

    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){    //注意这里的变量名要和前端一致
        log.info("page={},pageSize={}",page,pageSize);

        //这是一个分页构造器
        Page pageInfo = new Page(page,pageSize);

        //t条件构造器，做数据库查询用
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();

        //排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo,queryWrapper);

        return  R.success(pageInfo);
    }

}
