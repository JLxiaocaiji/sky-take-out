package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Result<List<DishVO>> list(Long categoryId) {
        // 在 redis 中查询
        String cachekey = "dish_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(cachekey);
        if( list != null && list.size() > 0) {
            return Result.success(list);
        }



        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        // 起售状态
        dish.setStatus(StatusConstant.ENABLE);

        list = dishService.listWithFlavor(dish);
        return Result.success(list);
    }
}
