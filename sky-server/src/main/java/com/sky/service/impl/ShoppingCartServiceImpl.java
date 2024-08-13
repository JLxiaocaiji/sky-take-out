package com.sky.service.impl;

import com.sky.dto.ShoppingCartDTO;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        // 判断当前插入到购物车中的商品是否已存在，
        /**
         * 情况1,传过来套餐信息：
         * 根据 userId 和 setmealId 从 shopping_cart 表中查是否已存在
         * select * from shopping_cart where user_id = ? and setmeal_id = xx
          */
        /**
         * 情况2, 传过来菜品信息：
         * 根据 userId 和 dishId; dish_flavor
         */



        // 若已存在，则只需数量 + 1


        // 若不存在，需要插入一条购物车数据
    }
}
