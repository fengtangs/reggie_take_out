package com.item.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.reggie.dto.DishDto;
import com.item.reggie.entity.Dish;
import com.item.reggie.entity.DishFlavor;
import com.item.reggie.mapper.DishMapper;
import com.item.reggie.service.DishFlavorService;
import com.item.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存口味数据
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {


        //首先保存菜品
        this.save(dishDto);

        Long dishId =dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> dishFlavors=dishDto.getFlavors();
        dishFlavors.stream().map((item)-> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据，到dish_flavor
        dishFlavorService.saveBatch(dishFlavors);


    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish=this.getById(id);
        DishDto records=new DishDto();
        BeanUtils.copyProperties(dish,records);


        //查询菜品对应口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        List<DishFlavor> list=dishFlavorService.list(queryWrapper);
        records.setFlavors(list);
        return records;

    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish基本信息
        this.updateById(dishDto);

        //更新dishflavor
        //1.delete
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //2.add

        Long dishId =dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> dishFlavors=dishDto.getFlavors();
        dishFlavors.stream().map((item)-> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据，到dish_flavor
        dishFlavorService.saveBatch(dishFlavors);

    }
}
