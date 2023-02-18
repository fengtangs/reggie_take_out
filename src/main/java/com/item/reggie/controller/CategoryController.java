package com.item.reggie.controller;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.entity.Category;
import com.item.reggie.entity.Employee;
import com.item.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import com.item.reggie.common.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save( @RequestBody Category category){
        log.info("新增菜品");
        categoryService.save(category);
        return R.success("新增分类");

    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page (int page, int pageSize){
        log.info("菜品： page={},pageSize={}",page,pageSize);

        //这是一个分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);

        //t条件构造器，做数据库查询用
        LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper<>();

        //排序条件
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询
        categoryService.page(pageInfo,queryWrapper);

        return  R.success(pageInfo);

    }

    /**
     * 根据id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("delete id={}",ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功！");
    }

    /**
     * 根据ID修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);
        categoryService.updateById(category);
        return R.success("修改信息成功");
    }

}
