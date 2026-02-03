## 招式 1：手里有清单，库里有库存，如何快速比对？(List 转 Map)

**【使用场景】**
比如：前端传了一堆要买的兵（清单），我要检查数据库里的兵（库存）够不够。
如果用双重 for 循环会很慢，要先把库存转成“字典”（Map）。

**【通用模板】**
```java
// 1. 先把数据库查出来的 List，转成 Map<ID, 数量>
Map<Integer, Long> inventoryMap = new HashMap<>();
for (UserTroopTbl item : listFromDb) {
    inventoryMap.put(item.getTroopId(), item.getCount());
}

// 2. 遍历前端传来的清单 (Map)
for (Map.Entry<Integer, Integer> entry : requestMap.entrySet()) {
    Integer targetId = entry.getKey();   // 目标ID
    Integer needCount = entry.getValue(); // 需要数量

    // 3. 查字典比对 (用 getOrDefault 防止空指针)
    Long haveCount = inventoryMap.getOrDefault(targetId, 0L);

    if (haveCount < needCount) {
        throw new RuntimeException("库存不足！ID:" + targetId);
    }
    
    // 4. 执行业务逻辑 (比如扣除)...
}
```

---

### 📝 招式 2：【防呆检查】(Fail Fast)

**【使用场景】** 在业务逻辑的最开始，先把所有**不可能的情况**（比如买负数个兵、用户不存在）全部拦住。这样后面的代码就可以放心大胆地跑，不用层层嵌套 `if-else`。

**【通用模板】**


```java
// 1. 检查参数是否合法
if (count <= 0) {
    throw new RuntimeException("数量必须大于0");
}

// 2. 检查数据是否存在
UserTbl user = userMapper.selectById(userId);
if (user == null) {
    throw new RuntimeException("查无此人: " + userId);
}

// 3. 检查权限/归属
if (!general.getUserId().equals(userId)) {
    throw new RuntimeException("这不是你的武将！");
}
```

---

### 📝 招式 3：【安全扣款】(CAS / SQL原子更新)

**【使用场景】** 当你涉及“钱”、“库存”等敏感数字的扣减时，**千万不要**先查出来，用 Java 减完再存回去（并发时会出 Bug）。 **最安全的做法：** 把判断逻辑写在 SQL 里，让数据库去保证原子性。

**【通用模板】** _Java 接口:_

```java
// 返回值 int 代表受影响的行数，0 表示扣减失败（余额不足）
int rows = userMapper.reduceGold(userId, cost);
if (rows == 0) {
    throw new RuntimeException("余额不足，扣款失败");
}
```

_Mapper XML:_


```xml
<update id="reduceGold">
    UPDATE users 
    SET gold = gold - #{cost} 
    WHERE id = #{userId} AND gold >= #{cost}  </update>
```

---

### 📝 招式 4：【数据搬运】(Entity 转 DTO)

**【使用场景】** 数据库查出来的对象（Entity）往往包含全部字段（如 `password`），不能直接给前端。 我们需要手动创建一个干净的对象（DTO），把能看的数据搬过去。

**【通用模板】**


```java
// 1. 准备好源数据 (Entity) 和 目标容器 (DTO)
UserTbl user = userMapper.selectById(userId);
PlayerInfoResp resp = new PlayerInfoResp();

// 2. 手动搬运 (清晰、可控)
resp.setId(user.getId());
resp.setNickname(user.getNickname());
// 密码字段就不搬运，前端就看不到了

// 3. 如果是列表，就用循环搬运
List<TroopDto> dtoList = new ArrayList<>();
for (UserTroopTbl t : dbList) {
    TroopDto dto = new TroopDto();
    dto.setId(t.getId());
    // ...
    dtoList.add(dto);
}
```

---