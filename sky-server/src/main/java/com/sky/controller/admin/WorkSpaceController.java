package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 工作台相关接口
 */
@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "工作台相关接口")
public class WorkSpaceController {

    @Autowired
    WorkSpaceService workSpaceService;

    /**
     * 根据时间段统计营业数据
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("工作台不同时间段数据查询")
    public Result<BusinessDataVO> businessData() {
        //获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单管理数据
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("查询订单管理数据")
    public Result<OrderOverViewVO> orderOverView() {
        return Result.success(workSpaceService.getOrderOverView());
    }

    /**
     * 查询菜品总览
     * @return
     */
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> dishOverView() {
        return Result.success(workSpaceService.getDishOverView());
    }

    /**
     * 查询套餐总览
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐总览")
    public Result<SetmealOverViewVO> setmealOverView() {
        return Result.success(workSpaceService.getSetmealOverView());
    }
}
