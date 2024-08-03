package com.sky.exception;

/**
 * 账号不存在异常
 */
//AccountNotFoundException 是 BaseException 的子类, 而 BaseException 是 RuntimeException 的子类
public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException() {
    }

    // 接受一个 String 类型的参数 msg
    public AccountNotFoundException(String msg) {
        // 调用了基类 BaseException 的构造函数，并将传入的消息 msg 传递给它。这意味着 BaseException 类应该有一个接受 String 参数的构造函数，用于初始化异常消息
        super(msg);
    }

}
