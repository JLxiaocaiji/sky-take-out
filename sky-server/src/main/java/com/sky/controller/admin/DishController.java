package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    public Result save(@RequestBody DishDTO dishDTO) {

        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }


    // 由于这种请求方式是 query 在 url 后拼接，所以不需要著名 @RequestBody
    @GetMapping("/page")
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    // 需要注意，起售中的菜品不能删除，可以一次删除一个菜品，也可批量删除；被套餐关联的菜品不能删除；删除菜品后，关联的口味数据也需要删除
    // 加了 @RequestParams 后，MVC会自动解析字符串并提取里面的 id

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除,{}", ids);
        dishService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 根据 id 查询菜品 且 带口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据 id 查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据 id 查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 根据 id 修改菜品基本信息和对应口味信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.updateWithFloor(dishDTO);
        return Result.success();
    }

    /**
     * 启用或停用菜品
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        // 1.启用; 0 停用
        log.info("启用或停用菜品：{}", id);
        dishService.startOrStop(status, id);
        return Result.success();
    }

    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }
}
