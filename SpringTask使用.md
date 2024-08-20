Spring Task使用步骤：
1. 导入 maven 坐标 spring-context
2. 启动类添加注解 @EnableScheduling 开启任务调度
3. 自定义定时任务类

4. cron
cron 是 一个字符串，可定义任务触发时间， 分为 6/7个区域，空格分开，分别代表：秒、分、时、日、月、周、年
```
2022年10月12日上午9点整:   0 0 9 12 10 ? 2022


Timer timer = new Timer()
TimeTask rask = new TimeTask() {
    public void run() {
        。。。
    }
}

timer.schedule(task, new SimpleDateFormat("ss mm HH dd MM ? yyyy").parse("0 0 12 * * ? 2023"))
```