package com.item.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.reggie.dto.DishDto;
import com.item.reggie.dto.SetmealDto;
import com.item.reggie.entity.Dish;
import com.item.reggie.entity.DishFlavor;
import com.item.reggie.entity.Setmeal;
import com.item.reggie.entity.SetmealDish;
import com.item.reggie.mapper.SetmealMapper;
import com.item.reggie.service.DishFlavorService;
import com.item.reggie.service.SetmealDishService;
import com.item.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    //新增套餐保存
    @Override
    public void saveWithFlavor(SetmealDto setmealDto) {
        //首先保存套餐
        this.save(setmealDto);

        Long Setmealid =setmealDto.getId();//套餐id

        //菜品
        List<SetmealDish> setmealDishes=setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)-> {
            item.setSetmealId(Setmealid);
            return item;
        }).collect(Collectors.toList());

        //保存套餐中的菜品数据，到dish_flavor
        setmealDishService.saveBatch(setmealDishes);

    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {

        //查询套餐基本信息
        Setmeal setmeal=this.getById(id);
        SetmealDto records=new SetmealDto();
        BeanUtils.copyProperties(setmeal,records);


        //查询套餐对应菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> list=setmealDishService.list(queryWrapper);
        records.setSetmealDishes(list);
        return records;
    }

    @Override
    public void updateWithdish(SetmealDto setmealDto) {
        //首先更新套餐
        this.updateById(setmealDto);

        Long Setmealid =setmealDto.getId();//套餐id

        //更新套餐的菜品信息
        //1.delete
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,Setmealid);

        setmealDishService.remove(queryWrapper);



        //菜品
        List<SetmealDish> setmealDishes=setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)-> {
            item.setSetmealId(Setmealid);
            return item;
        }).collect(Collectors.toList());

        //保存套餐中的菜品数据，到dish_flavor
        setmealDishService.saveBatch(setmealDishes);

    }

    @Override
    public void removeWithDish(Long[] ids) {



    }
}
