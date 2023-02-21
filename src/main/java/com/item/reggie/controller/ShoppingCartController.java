package com.item.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.reggie.common.BaseContext;
import com.item.reggie.common.R;
import com.item.reggie.dto.DishDto;
import com.item.reggie.dto.SetmealDto;
import com.item.reggie.entity.OrderDetail;
import com.item.reggie.entity.ShoppingCart;
import com.item.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        log.info(shoppingCart.toString());
        //设置用户id
        Long userid=BaseContext.getCurrentId();
        shoppingCart.setUserId(userid);

        //查询当前菜品是否在购物车中

        Long dishId =shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();

        if(dishId!=null){
            //添加到购物车的是菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else{
            //套餐
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        }
        //查询
        ShoppingCart shoppingCart1=shoppingCartService.getOne(lambdaQueryWrapper);

        //如果已经存在，数量+1；
        if(shoppingCart1!=null){
            Integer number=shoppingCart1.getNumber();
            shoppingCart1.setNumber(number+1);
            shoppingCartService.updateById(shoppingCart1);
        }
        //不存在就insert
        else{
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCart1=shoppingCart;
        }

        return R.success(shoppingCart1);

    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        log.info(shoppingCart.toString());
        //设置用户id
        Long userid=BaseContext.getCurrentId();
        shoppingCart.setUserId(userid);

        //查询当前菜品是否在购物车中

        Long dishId =shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();

        if(dishId!=null){
            //添加到购物车的是菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else{
            //套餐
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        }
        //查询
        ShoppingCart shoppingCart1=shoppingCartService.getOne(lambdaQueryWrapper);

        //数量-1；
        if(shoppingCart1!=null){
            Integer number=shoppingCart1.getNumber();
            shoppingCart1.setNumber(number-1);

            if(number-1<=0){
                shoppingCartService.removeById(shoppingCart1);
            }
            else shoppingCartService.updateById(shoppingCart1);
        }



        return R.success(shoppingCart1);

    }



    @GetMapping("/list")
    public R<List<ShoppingCart>> listR(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list=shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> delete(){
        log.info("clean");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());


        shoppingCartService.remove(queryWrapper);
        return R.success("成功");


    }

}
