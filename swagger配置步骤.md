1.导入 knife4j 的maven坐标
```
<dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
            <version>3.0.2</version>
</dependency>

```
2.在配置类中加入 knife4j 相关配置
```
@Bean
public Docket docket(){
    ApiInfo apiInfo = new ApiInfoBuilder()
           .title(“苍穹外卖项目接口文档”)
           .version(“2.0”)
           .description(“苍穹外卖项目接口文档")
           .build();
    Docket docket = new Docket(DocumentationType.SWAGGER_2)
           .apiInfo(apiInfo)
           .select()
           //指定生成接口需要扫描的包
           .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
           .paths(PathSelectors.any())
           .build();
    return docket;
```
3.设置静态资源映射，否则接口文档页面无法访问
```
/**
    * 设置静态资源映射
    * @param registry*/protected void addResourceHandlers(ResourceHandlerRegistry registry) {   log.info(“开始设置静态资源映射...");   registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");   registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");}

```


swagger 注解
1.@Api, 用在类上，例如Controller，表示对类的说明; 
2.@ApiModel, 用在类上，例如entity、DTO、VO
3.@ApiModelProperty, 用在属性上，描述属性信息
4.@ApiOperation, 用在方法上，例如Controller的方法，说明方法的用途、作用

```
@Api(tags = "员工相关接口")   // 用在 类 上
public class EmployeeController {
}

@ApiOperatioin(value = "员工登录")  // 用在 方法 上
publiuc Result<EmployeeLoginVO> login(xxx) {
}


@ApiModel("用户信息")   // 用在 类 上
public class User {
    @ApiModelProperty("用户ID")   // 用在 属性描述 上
    private String id;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("用户姓名")
    private String name;
}
```


