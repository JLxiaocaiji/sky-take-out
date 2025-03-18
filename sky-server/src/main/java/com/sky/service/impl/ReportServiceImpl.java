package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private WorkSpaceService workSpaceService;

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

    /**
     * 报表导出功能
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 1.查询数据库，获取近30天营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        // LocalDateTime.of(dateBegin, LocalTime.MIN) // 普通年月日带上 时分秒
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));


        /**
         * 2 .通过 POI 写入 excel 文件
         */
        // this.getClass(); // 获取类对象
        // getClassLoader(); // 返回一个 ClassLoader 对象，这是用于加载指定类的类加载器
        // getResourceAsStream();   // 从此类路径下来读取资源，返回一个 InputStream 对象，如果找不到资源则返回 null
        // ("template/运营数据报表模板");    // 在 template 目录下的 运营数据报表模板 文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");


        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取标签为 “” 的 sheet
            XSSFSheet sheet = excel.getSheet("sheet1");

            // 获取 sheet1 中的第二行，第二列并设值
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            // 获得第 4 行，第 3 列并设值
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            // 获得第 5 行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());


            // 填充明细数据
            for ( int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                // 查询某一天数据
                BusinessDataVO businessData = workSpaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                // 获得当前天的下一天
                row = sheet.getRow( 7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            /**
             * 3.通过输出流下载到浏览器
             */
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            /**
             * 关闭资源
             */
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map<String, Object> map = new HashMap();
        map.put("status", status);
        map.put("begin", beginTime);
        map.put("end", endTime);
        return orderMapper.countByMap(map);
    }
}
