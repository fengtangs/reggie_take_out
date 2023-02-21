package com.item.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.reggie.entity.Orders;

public interface OrderService extends IService<Orders>{

    public void submit(Orders orders,Long userid);
}
