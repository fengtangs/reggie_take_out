package com.item.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.common.CustmoException;
import com.item.reggie.common.R;
import com.item.reggie.dto.DishDto;
import com.item.reggie.entity.*;
import com.item.reggie.filter.CheckFilter;
import com.item.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * 菜品管理
 *
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;


    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @Value("${reggie.path}")
    private  String  basepath;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */

    @PostMapping
    public R<String> save(HttpServletRequest httpServletRequest,@RequestBody DishDto dishDto){
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info(dishDto.toString());

            dishService.saveWithFlavor(dishDto);

            return R.success("新增菜品成功保存");

        }
        return R.error("NOTLOGIN");


    }

    /**
     * 获取分页信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */


    @GetMapping("/page")
    public R<Page> page(HttpServletRequest httpServletRequest,int page, int pageSize,String name){    //注意这里的变量名要和前端一致
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info("page={},pageSize={},name={}",page,pageSize,name);

            //这是一个分页构造器
            Page pageInfo = new Page(page,pageSize);
            Page<DishDto> dishDtoPage=new Page<>();


            //t条件构造器，做数据库查询用
            LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();

            queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
            //排序条件
            queryWrapper.orderByDesc(Dish::getUpdateTime);

            //执行查询
            dishService.page(pageInfo,queryWrapper);

            //对象copy
            BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

            List<Dish> records=pageInfo.getRecords();
            List<DishDto> dishDtos=records.stream().map((item)->{
                DishDto dishDto=new DishDto();

                BeanUtils.copyProperties(item,dishDto);
                Long categoryId= item.getCategoryId();//拿到菜品分类id
                //根据id，查询名称
                Category category1= categoryService.getById(categoryId);
                if(category1!=null){
                    String categoryname=category1.getName();
                    dishDto.setCategoryName(categoryname);

                }
                return  dishDto;
            }).collect(Collectors.toList());



            dishDtoPage.setRecords(dishDtos);
            return  R.success(dishDtoPage);

        }
        return R.error("NOTLOGIN");


    }

    /**
     * 根据id停售启售
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> setstatus(HttpServletRequest httpServletRequest,@PathVariable int status, @RequestParam Long[] ids){

        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info("status={},id={}",status,ids);


            UpdateWrapper<Dish> updateWrapper=new UpdateWrapper<>();


            updateWrapper.set("status",status).in("id",ids);

            dishService.update(updateWrapper);
            if(status==0)
                return R.success("停售成功！");
            return R.success("启售成功！");

        }
        return R.error("NOTLOGIN");

        }


    /**
     * 根据id查询菜品信息和口味信息；
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(HttpServletRequest httpServletRequest,@PathVariable  Long id){
        if(CheckFilter.checkemployee(httpServletRequest)) {

            DishDto dishDto=dishService.getByIdWithFlavor(id);
            return R.success(dishDto);
        }
        return R.error("NOTLOGIN");

    }

    /**
     * 根据ID修改菜品和对应口味信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest httpServletRequest,@RequestBody DishDto dishDto){
        if(CheckFilter.checkemployee(httpServletRequest)) {

            log.info("修改分类信息：{}",dishDto);
            dishService.updateWithFlavor(dishDto);
            return R.success("修改信息成功");
        }
        return R.error("NOTLOGIN");

    }


    /**
     * 根据id删除菜品，同时删除对应的口味信息
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String > delete( HttpServletRequest httpServletRequest,@RequestParam Long[] ids){
        if(CheckFilter.checkemployee(httpServletRequest)) {

            log.info("ids={}",ids);

            int length= ids.length;
            int flag=0;
            List<Long> new_ids=new ArrayList<>();

            for(Long id:ids){
                //首先查询是否在套餐中
                LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
                dishLambdaQueryWrapper.eq(SetmealDish::getDishId,id);
                int count= setmealDishService.count(dishLambdaQueryWrapper);
                log.info("count:"+String.valueOf(count));
                if(count>0){
                    flag++;
                }
                else {
                    new_ids.add(id);
                }


            }

            log.info(new_ids.toString());


            LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();

            queryWrapper.in(DishFlavor::getDishId,new_ids);
            dishFlavorService.remove(queryWrapper);

            LambdaQueryWrapper<Dish> dishqueryWrapper=new LambdaQueryWrapper<>();
            dishqueryWrapper.in(Dish::getId,new_ids);
            List<Dish> list= dishService.list(dishqueryWrapper);


            for (int i=0;i<list.size();i++){
                File dir=new File(basepath+list.get(i).getImage());
                if(dir.exists()){
                    dir.delete();
                    log.info(String.valueOf(list.get(i)));
                }

            }
            dishService.removeByIds(new_ids);
            if (flag==0)
                return R.success("删除成功");
            else if (flag<length){
                return R.success("部分菜品删除成功，还有"+String.valueOf(length-flag)+"个删除失败，因为关联了套餐");
            }
            else {
                return R.error("全部删除失败，因为关联了套餐");
            }
        }
        return R.error("NOTLOGIN");

    }


//    @GetMapping("/list")
//    public R<List<Dish>>  get_list( Dish dish){
//        log.info(dish.toString());
//        List<Dish> dishList=new ArrayList<>();
//
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()),Dish::getName,dish.getName());
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//
//
//        //只查询没有停售的。
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //排序
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        dishList=dishService.list(queryWrapper);
//
//        return R.success(dishList);
//    }


    @GetMapping("/list")
    public R<List<DishDto>>  get_list( Dish dish){
        log.info(dish.toString());
        List<Dish> dishList=new ArrayList<>();

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()),Dish::getName,dish.getName());
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());


        //只查询没有停售的。
        queryWrapper.eq(Dish::getStatus,1);

        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        dishList=dishService.list(queryWrapper);

        List<DishDto> dishDtoList=dishList.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId= item.getCategoryId();//拿到菜品分类id
            //根据id，查询名称
            Category category1= categoryService.getById(categoryId);
            if(category1!=null){
                String categoryname=category1.getName();
                dishDto.setCategoryName(categoryname);

            }

            Long dishId=item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList=dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return  dishDto;
        }).collect(Collectors.toList());


        return R.success(dishDtoList);
    }

}
