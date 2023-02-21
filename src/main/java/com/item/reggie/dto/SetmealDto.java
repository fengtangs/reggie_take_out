package com.item.reggie.dto;


import com.item.reggie.entity.Setmeal;
import com.item.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
