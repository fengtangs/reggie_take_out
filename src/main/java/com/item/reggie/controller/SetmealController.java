package com.item.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.item.reggie.common.R;
import com.item.reggie.dto.DishDto;
import com.item.reggie.dto.SetmealDto;
import com.item.reggie.entity.*;
import com.item.reggie.filter.CheckFilter;
import com.item.reggie.service.CategoryService;
import com.item.reggie.service.DishService;
import com.item.reggie.service.SetmealDishService;
import com.item.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Value("${reggie.path}")
    private  String  basepath;
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(HttpServletRequest httpServletRequest,int page, int pageSize, String name) {    //注意这里的变量名要和前端一致

        if(CheckFilter.checkemployee(httpServletRequest)) {
            Page pageinfo = new Page(page, pageSize);
            Page<SetmealDto> dtopage=new Page<>();

            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

            //条件模糊查询
            queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);

            //排序
            queryWrapper.orderByDesc(Setmeal::getUpdateTime);

            setmealService.page(pageinfo, queryWrapper);
            //对象copy
            BeanUtils.copyProperties(pageinfo,dtopage,"records");


            List<Setmeal> records=pageinfo.getRecords();

            List<SetmealDto> setmealDtoList=records.stream().map((item)->{
                SetmealDto setmealDto=new SetmealDto();
                BeanUtils.copyProperties(item,setmealDto);

                Long categoryId=item.getCategoryId();
                Category category=categoryService.getById(categoryId);
                if(category!=null){
                    String categoryname=category.getName();
                    setmealDto.setCategoryName(categoryname);
                }
                return setmealDto;

            }).collect(Collectors.toList());

            dtopage.setRecords(setmealDtoList);
            return R.success(dtopage);
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


            UpdateWrapper<Setmeal> updateWrapper=new UpdateWrapper<>();


            updateWrapper.set("status",status).in("id",ids);

            setmealService.update(updateWrapper);
            if(status==0)
                return R.success("停售成功！");
            return R.success("启售成功！");
        }
        return R.error("NOTLOGIN");

    }


    /**
     * 保存新建的套餐
     *
     * @param setmealDto
     * @return
     */

    @PostMapping
    public R<String> save(HttpServletRequest httpServletRequest,@RequestBody SetmealDto setmealDto){

        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info(setmealDto.toString());

            setmealService.saveWithFlavor(setmealDto);

            return R.success("新增套餐成功保存");
        }
        return R.error("NOTLOGIN");





    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     * @return
     */

    @PutMapping
    public R<String> change_save(HttpServletRequest httpServletRequest,@RequestBody SetmealDto setmealDto){

        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info(setmealDto.toString());

            setmealService.updateWithdish(setmealDto);

            return R.success("修改成功");

        }
        return R.error("NOTLOGIN");




    }



    /**
     * 根据id删除套餐，同时删除对应的套餐菜品信息，只能删除停售的套餐
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String > delete( HttpServletRequest httpServletRequest,@RequestParam Long[] ids){
        if(CheckFilter.checkemployee(httpServletRequest)) {
            log.info("ids={}",ids);
            int flag=0;
            int length= ids.length;
            List<Long> new_ids=new ArrayList<>();

            //逐个处理
            for(Long id:ids){
                LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();

                queryWrapper.eq(Setmeal::getId,id);
                Setmeal setmeal=setmealService.getById(id);
                //如果是停售售状态
                if(setmeal.getStatus().equals(0) ){
                    File dir=new File(basepath+setmeal.getImage());
                    if(dir.exists()){
                        dir.delete();
                    }

                    new_ids.add(id);

                }
                //在售，
                else{
                    flag++;
                    log.info(String.valueOf(setmeal.getStatus()));
                }



            }
            log.info(new_ids.toString());
            if(flag<length){
                setmealService.removeByIds(new_ids);
                LambdaQueryWrapper<SetmealDish> dishqueryWrapper=new LambdaQueryWrapper<>();
                dishqueryWrapper.in(SetmealDish::getSetmealId,new_ids);

                setmealDishService.remove(dishqueryWrapper);

            }

            if(flag==0)
                return R.success("全部删除成功");
            else if (flag<length) {
                return R.success("部分删除成功，还有"+String.valueOf(length-flag)+"个未删除，因为是在售状态！");
            }
            else {
                return R.error("删除失败！套餐为在售状态！");
            }
        }
        return R.error("NOTLOGIN");

    }



    /**
     * 根据id查询菜品信息和口味信息；
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable  Long id){

        SetmealDto setmealDto=setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }


    /**
     * 根据条件查询
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> get_list(Setmeal setmeal){
        log.info(setmeal.toString());

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list=setmealService.list(queryWrapper);

        return R.success(list);
    }

}
