package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        // 新建一条菜品
        Dish dish = new Dish();

        // 属性拷贝
        BeanUtils.copyProperties(dishDTO, dish);


        // 向菜品插入1条数据
        dishMapper.insert(dish);

        // 获取 insert 语句生成的主键
        Long dishId = dish.getId();

        // 向口味插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if( flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 将集合整体插入
            dishFlavorMapper.batchInsert(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 注意 DTO 和 VO 的区别， DishVO 多出 categoryName 和 updateTime 属性

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    // 需要注意，起售中的菜品不能删除，可以一次删除一个菜品，也可批量删除；被套餐关联的菜品不能删除；删除菜品后，关联的口味数据也需要删除
    @Override
    public void batchDelete(List<Long> ids) {
        // 判断当前菜品是否能删除--->是否存在起售中的菜品
        for(Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if( dish.getStatus() == StatusConstant.ENABLE) {
                // 当前菜品处于起售状态，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }


        // 判断当前菜品是否能够删除--->是否被套餐关联
        // 根据菜品 id 查询 套餐 id, 能查到就说明被关联
        List<Long> setmealIds =  setmealDishMapper.getSetmealIdsByDishIds(ids);
        // 有点不对
        if (setmealIds != null && setmealIds.size() > 0) {
            // 当前菜品被套餐关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }


        // 删除菜品表中的数据
        for(Long id : ids) {
            dishMapper.deleteById(id);
            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }

        // 上面的优化 sql 执行语句
        // sql: delete from dish where id in (...)
        // 根据菜品 id 集合批量删除菜品数据
        dishMapper.deleteByIds(ids);

        // 根据菜品 id 集合批量删除 口味 数据
        // sql: delete from dish_flavor where dish_id in (...)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据 id 查询菜品
        Dish dish = dishMapper.getById(id);
        // 根据 id 查询口味
        List<DishFlavor> dishFlavor =  dishFlavorMapper.getByDishId(id);

        // 将查询到的数据封装到 VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO); // (source, target)
        dishVO.setFlavors(dishFlavor);

        return dishVO;
    }

    /**
     * 根据 id 修改菜品基本信息和对应口味信息
     * @param dishDTO
     * @return
     */
    @Override
    public void updateWithFloor(DishDTO dishDTO) {
        // 原本 dishDTO 中包含了数据 flavors,修改基本信息并不涉及到，所以 new 一个
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);


        // 修改菜品表基本信息
        dishMapper.update(dish);

        // 删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // 重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            // 向口味表插入 n 条数据
            dishFlavorMapper.batchInsert(flavors);
        }
    }
}
