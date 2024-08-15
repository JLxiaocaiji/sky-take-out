package com.sky.controller.notify;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.sky.service.OrderService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Api(tags = "支付回调相关接口")
@Slf4j
@RestController
@RequestMapping("/notify")
public class PayNotifyController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 支付成功回调
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/paySuccess")
    public void paySuccessNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 读取数据
        String body = readData(request);
        log.info("支付成功回调：{}", body);

        // 数据解密
        String plainText = decryptData(body);
        log.info("解密后的文本：{}", plainText);

        JSONObject jsonObject = JSON.parseObject(plainText);
        String outTradeNo = jsonObject.getString("out_trade_no");
        String transactionId = jsonObject.getString("transaction_id");

        log.info("商户平台订单号：{}", outTradeNo);
        log.info("微信支付交易号：{}", transactionId);

        // 业务处理，修改订单状态、来单提醒
        orderService.paySuccess(outTradeNo);

        // 给微信响应
        responseToWeixin(response);
    }


    /**
     * 读取数据
     * @param request
     * @return
     * @throws Exception
     */
    private String readData(HttpServletRequest request) throws Exception {
        // 从HttpServletRequest对象中获取一个BufferedReader，用于读取请求体中的文本数据
        BufferedReader reader = request.getReader();
        // 创建了一个StringBuilder对象，用于存储从请求体中读取的文本
        StringBuilder result = new StringBuilder();
        // 声明了一个String变量line，用于存储每次从BufferedReader读取的行
        String line = null;

        // 持续读取请求体的每一行
        while ((line = reader.readLine()) != null) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(line);
        }
        // 将StringBuilder对象转换为字符串
        return result.toString();
    }

    /**
     * 数据解密
     * @param body
     * @return
     * @throws Exception
     */
    private String decryptData(String body) throws Exception {
        JSONObject resultObject = JSON.parseObject(body);
        JSONObject resource = resultObject.getJSONObject("resource");

        String ciphertext = resource.getString("ciphertext");
        String nonce = resource.getString("nonce");
        String associatedData = resource.getString("associated_data");

        // getBytes(): 将字符串转换为字节数组
        AesUtil aesUtil = new AesUtil(weChatProperties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        // 密文解密
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);
        return plainText;
    }

    /**
     * 给微信响应
     * @param response
     * @throws Exception
     */
    private void responseToWeixin(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("code", "SUCCESS");
        map.put("message", "SUCCESS");
        response.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
        // toJSONString(map): 将Java Map对象转换为JSON格式的字符串
        // .getBytes(StandardCharsets.UTF_8): 将JSON字符串转换为UTF-8编码的字节数组
        // getOutputStream(): 获取HTTP响应的输出流
        // write(...): 方法将字节数组写入输出流
        response.getOutputStream().write(JSONUtils.toJSONString(map).getBytes(StandardCharsets.UTF_8));
        // flushBuffer(): 强制输出缓冲区中的所有内容，确保所有的响应数据都被发送到客户端
        response.flushBuffer();
    }
}
