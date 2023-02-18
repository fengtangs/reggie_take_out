package com.item.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.reggie.common.CustmoException;
import com.item.reggie.entity.Category;
import com.item.reggie.entity.Dish;
import com.item.reggie.entity.Setmeal;
import com.item.reggie.mapper.CategoryMapper;
import com.item.reggie.service.CategoryService;
import com.item.reggie.service.DishService;
import com.item.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {

        //查询当前分类是否关联了菜品，如果已经异常，抛出一个业务异常
        LambdaQueryWrapper<Dish>  dishLambdaQueryWrapper =new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(dishLambdaQueryWrapper);
        if(count>0){
            //已经关联
            throw  new CustmoException("当前分类下关联了菜品，不能删除");

        }

        //查询当前分类是否关联了套餐，如果已经异常，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2=setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0){
            //已经关联
            throw  new CustmoException("当前分类下关联了套餐，不能删除");
        }
        super.removeById(id);

    }
}
