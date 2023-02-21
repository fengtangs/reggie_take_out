package com.item.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.reggie.entity.Dish;
import com.item.reggie.entity.SetmealDish;
import com.item.reggie.mapper.DishMapper;
import com.item.reggie.mapper.SetmealDishMapper;
import com.item.reggie.service.DishService;
import com.item.reggie.service.SetmealDishService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
