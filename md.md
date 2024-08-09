1. 初始项目的整体结构
- sky-take-out: maven父工程，统一管理依赖版本，聚合其他子模块;
- sky-common: 子模块，存放公共类，例如：工具类、常量类、异常类等;
- sky-pojo: 子模块，存放实体类、VO、DTO等
```
Entity: 实体，通常和数据库中的表对应;
DTO: 数据传输对象，通常用于程序中各层之间传递数据;
VO: 视图对象，为前端展示数据提供的对象;
POJO: 普通Java对象，只有属性和对应的getter和setter
```
- sky-server: 子模块，后端服务，存放配置文件、Controller、Service、Mapper等 ;

sky-common 子模块中存放的是一些公共类，可以供其他模块使用


2. httpClient 依赖嵌套
<dependency>com.aliyun.oss</dependency> 中有 Httpclient 坐标，所以不用再次引入

3. get 请求
```
public void testGet() throws Exception {
    // 创建 httpClient 对象
    CloseableHttpClient httpClient = HttpClients.createDefault();


    // 创建请求对象
    HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");


    // 发送请求,接受响应结果
    CloseableHttpResponse response = httpClient.execute(httpGet);

    // 获取服务器返回的状态码
    int statusCode = response.getStatusLine().getStatusCode();
    System.out.println("服务端返回的状态码为：" + statusCode); // 200

    // 响应数据
    HttpEntity entity = response.getEntity();
    String body = EntityUtils.toString(entity);
    System.out.println("服务端返回的数据：" + body);

    // 关闭 response, 关闭资源
    response.close();
    httpClient.close();
}
```

4. post 请求
```
public void testPOST() throws Exception {
    // 创建 httpClient 对象
    CloseableHttpClient httpClient = HttpClients.createDefault();

    // 创建请求对象
    HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", "admin");
    jsonObject.put("password", "123456");

    StringEntity entity = new StringEntity(jsonObject.toString());
    // 编码方式
    entity.setContentEncoding("utf-8");
    // 数据格式
    entity.setContentType("application/json");
    httpPost.setEntity(entity);

    // 发送请求
    CloseableHttpResponse response = httpClient.execute(httpPost);

    // 解析返回结果
    int statusCode = response.getStatusLine().getStatusCode();
    System.out.println("响应码为：" + statusCode);   // 200

    HttpEntity entity1 = response.getEntity();
    String body = EntityUtils.toString(entity1);
    System.out.println("响应数据为：" + body);    // 整体返回结果

    // 关闭资源
    response.close();
    httpClient.close();
}
```

5. Spring Cache
是一个框架，实现了基于注解的缓存功能，只需要简单地加一个注解，就能实现缓存功能;提供了一层抽象，底层可以切换不同的缓存实现
5.1 @EnableCaching：开启缓存注解功能，通常加在启动类上
5.2 @Cacheable：在方法执行前先查询缓存中是否有数据，如果有数据，则直接返回缓存数据；如果没有缓存数据，调用方法并将方法返回值放到缓存中
5.3 @CachePut: 将方法的返回值放到缓存中; 
```
@CachePut(value = "userCache", key = "#user.id")    // 动态获取到 user.id 的字段值，最后会生成 userCache::user
public User updateUser(User user) {     // 这里的 形参 user 和 #user.id 的 user 保持一致
    // 更新用户信息的逻辑
    return userRepository.save(user);
}
value 属性指定了缓存名称
key 属性定义了缓存的键。这里使用了 SpEL 表达式 #user.id，它表示使用方法的 user 参数的 id 属性作为缓存键
```

5.4 @CacheEvict: 将一条或多条数据从缓存中删除



