package com.item.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.reggie.common.CustmoException;
import com.item.reggie.entity.*;
import com.item.reggie.mapper.OrderMapper;
import com.item.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    @Override
    public void submit(Orders orders, Long userid) {


        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,userid);
        List<ShoppingCart> shoppingCarts=shoppingCartService.list(shoppingCartLambdaQueryWrapper);

        if(shoppingCarts==null||shoppingCarts.size()==0){
            throw new CustmoException("购物车不能为空");

        }

        //查询用户数据
        User user=userService.getById(userid);

        //查询地址
        Long addressBookid= orders.getAddressBookId();
        AddressBook addressBook=addressBookService.getById(addressBookid);
        if(addressBook==null){
            throw new CustmoException("地址为空");
        }

        //生成订单号
        long orderid= IdWorker.getId();


        //金额
        AtomicInteger amount=new AtomicInteger(0);
       List<OrderDetail>orderDetailList= shoppingCarts.stream().map((item)->{
            OrderDetail orderDetail=new OrderDetail();
           orderDetail.setOrderId(orderid);
           orderDetail.setNumber(item.getNumber());
           orderDetail.setDishFlavor(item.getDishFlavor ());
           orderDetail.setDishId(item.getDishId());
           orderDetail.setSetmealId(item.getSetmealId());
           orderDetail.setName(item.getName());
           orderDetail.setImage(item.getImage());
           orderDetail.setAmount (item.getAmount ());
           amount.addAndGet (item.getAmount().multiply (new BigDecimal(item.getNumber())).intValue());

            return orderDetail;


        }).collect(Collectors.toList());



        //向订单表插入数据


        orders.setId(orderid);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userid);
        orders.setUserName(user.getName());
        orders.setNumber(String.valueOf(orderid));
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getProvinceName()==null?"": addressBook.getProvinceName()
                +addressBook.getCityName()==null?"": addressBook.getCityName()
                +addressBook.getDistrictName()==null?"": addressBook.getDistrictName()
                +addressBook.getDetail()==null?"":addressBook.getDetail()
        );
        this.save(orders);
        //向订单明细表插入数据

        orderDetailService.saveBatch(orderDetailList);

        //清空购物车数据
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }

    @Override
    public void reloadorders(Long orderId, Long UserId) {
                //查询到订单，获取订单详细信息
        Orders order= this.getById(orderId);
        //使用订单id查询到订单细节
        LambdaQueryWrapper<OrderDetail>orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetailList=orderDetailService.list(orderDetailLambdaQueryWrapper);
        //清空购物车，把订单重新放到购物车
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,UserId);
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);

//        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        for (OrderDetail tmp:orderDetailList){
            ShoppingCart shoppingCart=new ShoppingCart();

            shoppingCart.setId(IdWorker.getId());
            shoppingCart.setUserId(UserId);
            shoppingCart.setImage(tmp.getImage());
            shoppingCart.setDishId(tmp.getDishId());
            shoppingCart.setSetmealId(tmp.getSetmealId());
            shoppingCart.setDishFlavor(tmp.getDishFlavor());
            shoppingCart.setNumber(tmp.getNumber());
            shoppingCart.setAmount(tmp.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

    }
}
