package com.item.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.common.BaseContext;
import com.item.reggie.common.CustmoException;
import com.item.reggie.common.R;
import com.item.reggie.dto.DishDto;
import com.item.reggie.dto.OrdersDto;
import com.item.reggie.entity.*;
import com.item.reggie.filter.CheckFilter;
import com.item.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;


    /**
     * 获取分页数据
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(HttpServletRequest httpServletRequest,int page, int pageSize, Long number, String beginTime, String endTime) {    //注意这里的变量名要和前端一致

        if(CheckFilter.checkemployee(httpServletRequest)) {

            log.info("page={},pagesize={},number={},time1={},time2={}", page, pageSize, number, beginTime, endTime);

            Page pageInfo=new Page(page,pageSize);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.like(number!=null,Orders::getId,number);
            queryWrapper.ge(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime).le(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

            queryWrapper.orderByDesc(Orders::getOrderTime);

            orderService.page(pageInfo,queryWrapper);


            return R.success(pageInfo);
        }
        return R.error("NOTLOGIN");


    }


    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> order(HttpServletRequest request, @RequestBody Orders orders){
        log.info(orders.toString());

        //获取当前用户id
       Long userid = (Long) request.getSession().getAttribute("user");


       orderService.submit(orders,userid);


        return R.success("成功下单");

    }


    /**
     * 订单详情
     * @param page
     * @param pageSize
     * @return
     */


    @GetMapping("/userPage")
    public R<Page> page (HttpServletRequest httpServletRequest,int page, int pageSize){
        log.info("订单： page={},pageSize={}",page,pageSize);

        //这是一个分页构造器
        Page pageInfo = new Page(page,pageSize);
        Page<OrdersDto> ordersDtoPage=new Page<>();

        //获取用户id
        Long userid= (Long) httpServletRequest.getSession().getAttribute("user");

        //t条件构造器，做数据库查询用
        LambdaQueryWrapper<Orders> queryWrapper =new LambdaQueryWrapper<>();

        queryWrapper.eq(Orders::getUserId,userid);
        //排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //执行查询
        orderService.page(pageInfo,queryWrapper);

        //对象copy
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records=pageInfo.getRecords();
        List<OrdersDto> ordersDtos=records.stream().map((item)->{
            OrdersDto ordersDto=new OrdersDto();

            BeanUtils.copyProperties(item,ordersDto);
            String orderid= item.getNumber();//拿到订单号
            //根据订单号，查询名称

            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderid);
            List<OrderDetail> list =orderDetailService.list(orderDetailLambdaQueryWrapper);
            if(list!=null){
                ordersDto.setOrderDetails(list);

            }
            return  ordersDto;
        }).collect(Collectors.toList());



        ordersDtoPage.setRecords(ordersDtos);
        return  R.success(pageInfo);

    }


    @PutMapping()
    public R<String> change_status(HttpServletRequest httpServletRequest,@RequestBody  Orders orders){
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info(orders.toString());
            Orders orders1=orderService.getById(orders.getId());
            if(orders1==null){
                throw new CustmoException("订单错误");
            }
            orders1.setStatus(orders.getStatus());
            orderService.updateById(orders1);
            return R.success("成功");

        }
        return R.error("NOTLOGIN");


    }


    @PostMapping("/again")
    public R<String> again(HttpServletRequest httpServletRequest,@RequestBody Orders orders){
        log.info("id={}",orders.getId());
//        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();



        //1.获取用户id
        Long userid= (Long) httpServletRequest.getSession().getAttribute("user");
        //查询到订单，获取订单详细信息
        Orders order= orderService.getById(orders.getId());
        //使用订单id查询到订单细节
        LambdaQueryWrapper<OrderDetail>orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orders.getId());
        List<OrderDetail> orderDetailList=orderDetailService.list(orderDetailLambdaQueryWrapper);
        //清空购物车，把订单重新放到购物车
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userid);
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);

//        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        for (OrderDetail tmp:orderDetailList){
            ShoppingCart shoppingCart=new ShoppingCart();

            shoppingCart.setId(IdWorker.getId());
            shoppingCart.setUserId(userid);
            shoppingCart.setImage(tmp.getImage());
            shoppingCart.setDishId(tmp.getDishId());
            shoppingCart.setSetmealId(tmp.getSetmealId());
            shoppingCart.setDishFlavor(tmp.getDishFlavor());
            shoppingCart.setNumber(tmp.getNumber());
            shoppingCart.setAmount(tmp.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }



        return R.success("success");


    }
}
