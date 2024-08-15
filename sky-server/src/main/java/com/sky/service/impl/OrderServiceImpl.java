package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    // 订单表
    @Autowired
    private OrderMapper orderMapper;

    // 订单明细表
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    // 地址簿
    @Autowired
    private AddressBookMapper addressBookMapper;

    // 购物车
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    // 用户
    @Autowired
    private UserMapper userMapper;

    // webSocketServer

    // 微信支付
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();

        /**
         * 1.处理各种业务异常(地址为空，购物车数据为空)
          */
        // 1.1 地址为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if( addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 1.2 购物车数据为空
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if(shoppingCartList == null || shoppingCartList.size() == 0) {
            // 抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        /**
         * 2. 向订单表插入一条数据; 补全各个属性
         */
        Orders orders = new Orders();
        // 传入参数进行拷贝
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());   // 订单时间
        orders.setPayStatus(Orders.UN_PAID);    // 订单支付状态：未支付
        orders.setStatus(Orders.PENDING_PAYMENT);   // 订单状态：等待支付
        // 订单号：使用当前系统时间戳
        orders.setNumber(String.valueOf(System.currentTimeMillis()));  // 订单编号：string 类型的当前时间
        // 用户手机号：从地址簿中查询
        orders.setPhone(addressBook.getPhone());
        // 收货人：地址簿中查询
        orders.setConsignee(addressBook.getConsignee());
        // 设置订单发起用户
        orders.setUserId(userId);

        orderMapper.insert(orders);

        /**
         * 3. 向订单明细表插入 n 条数据
         */
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for( ShoppingCart cart: shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);

            // orders 中的 id 是在 orderMapper 插入后获得的
            // 设置当前订单明细关联订单 id
            orderDetail.setOrderId(orders.getId());
            // 放入到集合中，一次性批量插入
            orderDetailList.add(orderDetail);
        }

        // 一次性批量插入
        orderDetailMapper.batchInsert(orderDetailList);

        // 4. 清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);

        // 5. 封装 VO 返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户 id
        Long userId = BaseContext.getCurrentId();
        // 根据用户id查询用户
        User user = userMapper.getById(userId);

        // 调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                // 商户订单号
                ordersPaymentDTO.getOrderNumber(),
                // 支付金额，单位 元
                new BigDecimal(0.01),
                // 商品描述
                "苍穹外卖订单",
                // 微信用户的openid
                user.getOpenid()
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        // Fastjson库将一个JSON对象转换为一个Java对象
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));


        // 直接调用paySuccess方法，模拟支付成功
        paySuccess(ordersPaymentDTO.getOrderNumber());

        return null;
//        return vo;
    }

    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder().id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过websocket通知商家
        Map<String, Object> map = new HashMap<>();
        // 1:来单通知
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);
        String msg = JSONObject.toJSONString(map);
//        webSocketServer.sendToAllClient(msg);
    }
}
