package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        // 判断当前插入到购物车中的商品是否已存在，

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // 拿到当前用户 id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 数据库查询
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        /**
         * 情况1,传过来套餐信息：
         * 根据 userId 和 setmealId 从 shopping_cart 表中查是否已存在
         * select * from shopping_cart where user_id = ? and setmeal_id = xx
          */


        /**
         * 情况2, 传过来菜品信息：
         * 根据 userId 和 菜品id: dishId; 口味: dish_flavor
         */



        // 若已存在，则只需数量 + 1, 对应 sql: update shopping_cart set number = ? where id = ?
        if( list != null && list.size() > 0) {
            // 对于相同商品，只需修改数量，所以取出第一条数据就可以
            ShoppingCart cart = list.get(0);
            // 获得原本数量，并且 + 1
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }
        // 若不存在，需要插入一条购物车数据
        else {
            // 判断传入的是 菜品 还是 套餐，判断 菜品id， 套餐id 是否为空
            Long dishId = shoppingCartDTO.getDishId();
            if ( dishId != null) {
                // 原本存在有 菜品
                Dish dish = dishMapper.getById(dishId);
                // 添加到购物车中
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            } else {
                // 原本存在有 套餐

                // 查取套餐表，获取 id
                Long setmealId = shoppingCartDTO.getSetmealId();

                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);

        }


    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        // 获取当前用户 id
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(userId)
                                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        // 获取当钱微信用户 id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
