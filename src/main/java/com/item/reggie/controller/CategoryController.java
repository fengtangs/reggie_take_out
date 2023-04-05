package com.item.reggie.controller;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.dto.DishDto;
import com.item.reggie.entity.Category;
import com.item.reggie.entity.Employee;
import com.item.reggie.filter.CheckFilter;
import com.item.reggie.service.CategoryService;
import com.item.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import com.item.reggie.common.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;



    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest httpServletRequest, @RequestBody Category category){
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info("新增菜品");
            categoryService.save(category);
            return R.success("新增分类");
        }
        return R.error("NOTLOGIN");


    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page (HttpServletRequest httpServletRequest, int page, int pageSize){
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info("菜品： page={},pageSize={}",page,pageSize);

            //这是一个分页构造器
            Page<Category> pageInfo = new Page<>(page,pageSize);

            //t条件构造器，做数据库查询用
            LambdaQueryWrapper<Category> queryWrapper =new LambdaQueryWrapper<>();

            //排序条件
            queryWrapper.orderByAsc(Category::getUpdateTime);

            //执行查询
            categoryService.page(pageInfo,queryWrapper);

            return  R.success(pageInfo);
        }
        return R.error("NOTLOGIN");


    }

    /**
     * 根据id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(HttpServletRequest httpServletRequest,Long ids){
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info("delete id={}", ids);
            categoryService.remove(ids);
            return R.success("分类信息删除成功！");
        }
        return R.error("NOTLOGIN");
    }

    /**
     * 根据ID修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest httpServletRequest, @RequestBody Category category){
        if(CheckFilter.checkemployee(httpServletRequest)){
            log.info("修改分类信息：{}",category);
            categoryService.updateById(category);
            return R.success("修改信息成功");
        }
      return R.error("NOTLOGIN");
    }


    /**
     * 根据条件查询分类数据
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category > queryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list=categoryService.list(queryWrapper);

        return R.success(list);
    }



}
