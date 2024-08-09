启动：cmd + redis-server.exe redis.windows.conf;
连接 redis 服务：cmd + redis-cli.exe;
退出： exit;
-h localhost 指定主机名，-p 6370 指定端口号: redis-cli.exe -h localhost -p 6370

redis 常用数据类型：
1. 字符串
```
SET key value		    # 设置指定key的值
GET key			        # 获取指定key的值
SETEX key seconds value	# 设置指定key的值，并将 key 的过期时间设为 seconds 秒，例如：验证码，设置5分钟有效, SETEX useable 60 123: 设置 useable 的值为 123，有效时长 60 秒
SETNX key value		    # 只有在 key 不存在时设置 key 的值

```
2. Hash
```
HSET key field value 	# 将哈希表 key 中的字段 field 的值设为 value
HGET key field 	        # 获取存储在哈希表中指定字段的值
HDEL key field		    # 删除存储在哈希表中的指定字段
HKEYS key 		        # 获取哈希表中所有字段
HVALS key 		        # 获取哈希表中所有值

```
3. List  Left/LIST 首字母 L, Right 首字母 R; 可以有重复元素
```
LPUSH key value1 [value2] 	# 将一个或多个值插入到列表头部(左边)
LRANGE key start stop 		# 获取列表指定范围内的元素  例如：lrange xxx 0 -1 获取 整条元素
RPOP key 			        # 移除并获取列表最后一个元素(右边)
LLEN key 			        # 获取列表长度

```
4. Set  没有重复元素
```
SADD key member1 [member2] 	# 向集合添加一个或多个成员     s+add
SMEMBERS key 		        # 返回集合中的所有成员         s+members
SCARD key 			        # 获取集合的成员数            s+card
SINTER key1 [key2] 		    # 返回给定所有集合的交集       s+inter
SUNION key1 [key2] 		    # 返回所有给定集合的并集       s+union
SREM key member1 [member2] 	# 删除集合中一个或多个成员      s+remove

```
5. Zset / sorted Set   没有重复元素，每个元素都会关联一个double类型的分数
```
ZADD key score1 member1 [score2 member2] 	# 向有序集合添加一个或多个成员
ZRANGE key start stop [WITHSCORES] 		    # 通过索引区间返回有序集合中指定区间内的成员, WITHSCORES 是否带分数
ZINCRBY key increment member 			    # 有序集合中对指定成员的分数加上增量 increment;  ZINCRBY zset 5.0 a: 给 a 加上分数 5.0， 以此改变排序
ZREM key member [member ...] 			    # 移除有序集合中的一个或多个成员
```

6. redis 通用命令
```
KEYS pattern 		# 查找所有符合给定模式( pattern)的 key 
EXISTS key 		    # 检查给定 key 是否存在
TYPE key 		    # 返回 key 所储存的值的类型
DEL key 		    # 该命令用于在 key 存在是删除 key

```

7.步骤:
导入Spring Data Redis 的maven坐标
配置Redis数据源
编写配置类，创建RedisTemplate对象
通过RedisTemplate对象操作Redis

8. String 示例
```
// set
redisTemplate.opsForValue().set("city", "北京");
// GET
String city = (String) redisTemplate.opsForValue().get("city");
System.out.println(city);
// SETEX 命令
redisTemplate.opsForValue().set("code", "1", 3, TimeUnit.MINUTES);

// SETNX 命令
// 在此传入的是 obj 类型，传入的对象是 value 这个参数， 给任意对象都会转化为 String
redisTemplate.opsForValue().setIfAbsent("lock", "1");   // 成功
redisTemplate.opsForValue().setIfAbsent("lock", "2");   // 失败
```

9. List
```
// List 操作
ListOperations listOperations = redisTemplate.opsForList();

// LPUSH key value1 [value2]
listOperations.leftPushAll("mylist", "1", "2", "3");
listOperations.leftPushAll("mylist", "4");

// LRANGE key start stop
List mylist = listOperations.range("mylist", 0, -1);
System.out.println(mylist);

// RPOP key
listOperations.rightPop("mylist");

// LLEN key
Long size = listOperations.size("mylist");
System.out.println(size);
```

10. Set
```
// 集合 没有重复元素
SetOperations setOperations = redisTemplate.opsForSet();

// SADD key member1 [member2]
setOperations.add("set1", "a", "b");
setOperations.add("set1", "c", "d");

// SMEMBERS key
Set members = setOperations.members("set1");
System.out.println(members);

// SCARD key
Long size = setOperations.size("set1");
System.out.println(size);

// SINTER key1 [key2]
Set intersect = setOperations.intersect("set1", "set2");
System.out.println(intersect);

// SUNION key1 [key2]
Set union = setOperations.union("set1", "set2");
System.out.println(union);

// SREM key member1 [member2]
setOperations.remove("set1", "set2");
```

11. Zset
```
// Zset 有序集合
ZSetOperations zSetOperations = redisTemplate.opsForZSet();

// ZADD key score1 member1 [score2 member2]
zSetOperations.add("zset1", "a", 1);
zSetOperations.add("zset1", "b", 2);
zSetOperations.add("zset1", "c", 3);

// ZRANGE key start stop [WITHSCORES]
Set zset1 = zSetOperations.range("zset1", 0, -1);
System.out.println(zset1);

// ZINCRBY key increment member
zSetOperations.incrementScore("zset1", "c", 10);

// ZREM key member [member ...]
zSetOperations.remove("zset1", "a", "b");
```

12. 常用命令
```
// KEYS pattern 
Set keys = redisTemplate.keys("*");
System.out.println(keys);

// EXISTS key
Boolean name = redisTemplate.hasKey("name");
Boolean set1 = redisTemplate.hasKey("set1");

for ( Object key : keys) {
    // TYPE key 
    DataType type = redisTemplate.type("name");
    System.out.println(type.name());
}

// DEL key 
redisTemplate.delete("mylist");
```

13. redis 清理
- 1.单个清理
```
@Autowired
private RedisTemplate redisTemplate
String key = "dish_" + dishDTO.getCategoryId();
redisTemplate.delete(key)
```
- 2.批量清理
```
// 将所有菜品缓存清楚，所有 dish_ 开头的 key
Set key = redisTemplate.keys("dish_")
redisTemplate.delete(keys)


// 可构造私有方法
private void cleanCache(String pattern) {
    Set keys = redisTemplate.keys(pattern);
    redisTemplate.delete(keys);
}
```

14. Spring Cache
    key 属性定义了缓存的键。这里使用了 SpEL 表达式 #user.id，它表示使用方法的 user 参数的 id 属性作为缓存键
