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
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {

        // 构造 redis 中的key, 规则： dish_ 分类 id
        String key = "dish_" + categoryId;

        // 查询 redis 中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);

        if (list != null && list.size() > 0) {
            // 若存在，直接返回，不查询数据库
            return Result.success(list);
        }

        // 构造一个 dish
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        // 起售状态
        dish.setStatus(StatusConstant.ENABLE);

        // 若不存在，查询数据库，缓存到 redis
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);


        return Result.success(list);
    }
}
