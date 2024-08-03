package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {
    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);


    // 需要注意，起售中的菜品不能删除，可以一次删除一个菜品，也可批量删除；被套餐关联的菜品不能删除；删除菜品后，关联的口味数据也需要删除
    /**
     * 菜品批量删除
     * @param ids
     */
    void batchDelete(List<Long> ids);

    /**
     * 根据 id 查询菜品 且 带口味
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 根据 id 修改菜品基本信息和对应口味信息
     * @param dishDTO
     * @return
     */
    void updateWithFloor(DishDTO dishDTO);
}
