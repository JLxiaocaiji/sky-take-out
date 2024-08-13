package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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



    /**
     * 批量插入
     * @param setmealDishes
     */
    void batchInsert(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除
     *
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getDishBySetmealId(Long id);
}
