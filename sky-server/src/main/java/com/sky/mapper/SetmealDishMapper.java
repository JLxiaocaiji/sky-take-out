package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品 id获取 套餐 id
     * @param dishIds
     * @return
     */
    // select setmea_id from setmeal_dish where dish_id in (ids)
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
