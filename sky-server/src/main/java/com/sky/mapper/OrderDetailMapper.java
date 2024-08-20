package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {


    /**
     * 批量插入 订单明细
     * @param orderDetailList
     */
    void batchInsert(List<OrderDetail> orderDetailList);

    /**
     * 查询订单菜品详情信息（订单中的菜品和数量）
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long id);
}
