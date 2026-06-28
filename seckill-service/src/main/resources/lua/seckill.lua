-- KEYS[1] = 库存key, 例如 seckill:stock:1
-- KEYS[2] = 用户集合key, 例如 seckill:users:1
-- ARGV[1] = 用户ID

local stock = redis.call('get', KEYS[1])
local numStock = tonumber(stock)  -- 转为数字，若转换失败则为 nil
if numStock == nil then
    return -3   -- 表示 stock 不是有效数字（可能为 nil、空字符串或带引号）
end

if numStock <= 0 then
    return -1   -- 库存不足
end

if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -2  -- 已购买
end

redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])
return 1  -- 成功