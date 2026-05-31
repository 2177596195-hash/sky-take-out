package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface DishService {

    /*
    * 新增菜品*/
    public void saveWithFlavor(DishDTO dishDTO);

    /*
    * 菜品分页查询*/
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*
    * 菜品的批量删除*/
    void deleteBatch(List<Long> ids);
}
