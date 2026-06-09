package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
 * 菜品管理*/
@RequestMapping("/admin/dish")
@ApiOperation("菜品管理相关接口")
@RestController
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /*
     * 新增菜品*/
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //将所有菜品缓存数据删除，所有以dish_开头的key都删除
        String key = "dish_" + dishDTO.getCategoryId();
        clearCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
     * 菜品批量删除*/
    @ApiOperation("批量删除")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除：{}", ids);
        dishService.deleteBatch(ids);

        //将所有菜品缓存数据删除，所有以dish_开头的key都删除
        clearCache("dish_*");
        return Result.success();
    }

    /*
     * 根据id查询菜品信息*/
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品信息")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品信息：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /*
     * 修改菜品信息*/
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品信息：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //将所有菜品缓存数据删除，所有以dish_开头的key都删除
        clearCache("dish_*");

        return Result.success();
    }

    /*
    * 菜品起售停售*/
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {

        log.info("菜品起售停售：{}", status, id);
        dishService.startOrStop(status, id);

        //将所有菜品缓存数据删除，所有以dish_开头的key都删除
        clearCache("dish_*");
        return Result.success();
    }

    /*
    *根据分类id查询菜品 */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /*
    * 清理缓存数据 */
    private void clearCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
