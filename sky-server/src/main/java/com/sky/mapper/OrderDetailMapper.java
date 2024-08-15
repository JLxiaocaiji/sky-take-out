package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {


    /**
     * 批量插入 订单明细
     * @param orderDetailList
     */
    void batchInsert(List<OrderDetail> orderDetailList);
}
