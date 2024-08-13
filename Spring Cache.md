Spring Cache
是一个框架，实现了基于注解的缓存功能，只需要简单地加一个注解，就能实现缓存功能;提供了一层抽象，底层可以切换不同的缓存实现
1. @EnableCaching：开启缓存注解功能，通常加在启动类上
2. @Cacheable：在方法执行前先查询缓存中是否有数据，如果有数据，则直接返回缓存数据；如果没有缓存数据，调用方法并将方法返回值放到缓存中
```
@GetMapping
// 注意不能写 key = "result.xx", 没有 result 这个关键字，只能写对应形参 id
@Cacheable(cacheNames = "userCache", key = "#id")   // key的生成：userCache::id
public User getById(Long id){
    User user = userMapper.getById(id);
    return user;
}
```


3. @CachePut: 将方法的返回值放到缓存中;
```
@CachePut(value = "userCache", key = "#user.id")    // 动态获取到 user.id 的字段值，最后会生成 userCache::user
public User updateUser(User user) {     // 这里的 形参 user 和 #user.id 的 user 保持一致
    // 更新用户信息的逻辑
    return userRepository.save(user);
}
value 属性指定了缓存名称
key 属性定义了缓存的键。这里使用了 SpEL 表达式 #user.id，它表示使用方法的 user 参数的 id 属性作为缓存键

// 还可以使用关键字 result
    // @CachePut(cacheNames = "userCache", key = "#result.abc")
```
以下5种写法结果一致
```
@CachePut(cacheNames = "userCache", key = "#user.id")
@CachePut(cacheNames = "userCache", key = "#result.id")
// 不管是 p0, a0, 他们表示的都是一致的, 仅限于 p,a
@CachePut(cacheNames = "userCache", key = "#p0.id")
@CachePut(cacheNames = "userCache", key = "#a0.id")
// #root.args[0]: 当前方法的第一个参数
@CachePut(cacheNames = "userCache", key = "#root.args[0].id")
```


4. @CacheEvict: 将一条或多条数据从缓存中删除， allEntries = true： 清除指定缓存中的所有条目
```
@DeleteMapping
// key 的生成 userCache::key
@CacheEvict(cacheNames = "userCache", key = "#id")
public void deleteById(Long id){
    userMapper.deleteById(id);
}

@DeleteMapping("/delAll")
// allEntries = true：这个属性是一个布尔值，默认为 false。当设置为 true 时，它会指示 Spring 清除指定缓存中的所有条目。换句话说，这将清空 setmealCache 缓存中的所有数据
@CacheEvict(cacheNames = "userCache", allEntries = true )
public void deleteAll(){
    userMapper.deleteAll();
}
```

二、具体的实现思路如下：
1. 导入Spring Cache和Redis相关maven坐标
2. 在启动类上加入@EnableCaching注解，开启缓存注解功能
3. 在用户端接口SetmealController的 list 方法上加入@Cacheable注解
4. 在管理端接口SetmealController的 save、delete、update、startOrStop等方法上加入CacheEvict注解