package com.item.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.reggie.dto.SetmealDto;
import com.item.reggie.entity.Setmeal;
import com.item.reggie.entity.SetmealDish;

public interface SetmealService extends IService<Setmeal> {

    //保存新增的套餐
    public  void saveWithFlavor(SetmealDto setmealDto);

    //查询套餐信息
    public  SetmealDto getByIdWithDish(Long id);


    //修改套餐
    public  void updateWithdish(SetmealDto setmealDto);


    public void removeWithDish(Long[] ids);



}
