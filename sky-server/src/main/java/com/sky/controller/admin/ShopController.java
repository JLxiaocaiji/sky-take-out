package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {

    // 定义字符串常量
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态 到 redis
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status) {

        log.info("设置店铺营业状态为：{}", status == 1? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @GetMapping("/{status}")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        // redisTemplate.opsForValue().get("SHOP_STATUS")：这一行代码是从 Redis 缓存中获取键为 “SHOP_STATUS” 的值,
        // Integer status = (Integer) Optional.ofNullable(redisTemplate.opsForValue().get(KEY)).orElse(0);ptional.ofNullable 方法接受一个可能为 null 的值。如果传递的值是 null，它会返回一个空的 Optional 对象；否则，它会返回一个包含非 null 值的 Optional 对象;orElse 方法会在 Optional 对象为空时返回一个默认值，在这个例子中是 0
        log.info("设置店铺营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
