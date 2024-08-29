package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    @ApiOperation("营业额统计")
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {

        // 把 开始日期 到 结束日期 每天都存储
        List<LocalDate> dates = new ArrayList<>();
        dates.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dates.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for( LocalDate date: dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginTime);
            map.put("end", endTime);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

//        字符串日期, 字符串营业额
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dates, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    @ApiOperation("用户数据统计")
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while( !begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 新增用户数
        List<Integer> newUserList = new ArrayList<>();
        // 总用户数
        List<Integer> totalUserList = new ArrayList();

        for ( LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 新增用户数量 select count(id) from user where create_time > ?  and end_time <
            Integer newUser = getUserCount(beginTime, endTime);

            // 总用户数量 select count(id) from user where create_time <  ?
            Integer totalUser = getUserCount(null, endTime);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        // 将数组或集合中的元素以某种分隔符连接起来
        return UserReportVO.builder().dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ",")).build();
    }


    /**
     * 根据时间区间统计用户数量
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map<String, Object> map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);
        return userMapper.countByMap(map);
    }


    /**
     * 根据时间区间 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    @ApiOperation("根据时间区间 订单统计")
    public OrderReportVO getOrderStatics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while( !begin.equals(end)) {
            begin = begin.plusDays(1);
        }

        // 每天订单总数集合
        List<Integer> orderCountList = new ArrayList<>();

        // 每天有效订单数集合
        List<Integer> validOrderCountList = new ArrayList<>();

        for( LocalDate date: dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 所有订单
            // select count(id) from orders where order_time > beginTime and order_time < endTime
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            // 有效订单
            // select count(id) from orders where order_time > begin_time and order_time < end_time and status == 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            // 无效订单
            // select count(id) from orders where order_time > begin_time and order_time < end_time and status ==
            Integer invalidOrderCount = getOrderCount(beginTime, endTime, Orders.CANCELLED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        // 订单总数
        Integer totalOrderCount = orderCountList.stream().reduce((a, b) -> a + b).get();
        //  Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        // 有效订单
        Integer validOrderCount = validOrderCountList.stream().reduce((a, b) -> a + b).get();

        // 订单完成率
        Double orderCompletionRate = 0.0;
        if ( totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * top10 订单
     * @param begin
     * @param end
     * @return
     */
    @Override
    @ApiOperation("top10 订单")
    // 注意要先从 orderDetail 中查到所有订单，且要 右关联 状态表,找到所有已完成的订单
    public SalesTop10ReportVO getTop10orderStatics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        // 转化为 SalesTop10ReportVO 输出
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map<String, Object> map = new HashMap();
        map.put("status", status);
        map.put("begin", beginTime);
        map.put("end", endTime);
        return orderMapper.countByMap(map);
    }
}
