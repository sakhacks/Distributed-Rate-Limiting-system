-- KEYS[1] = the Redis key, e.g. "ratelimit:token_bucket:{apiKey}"
-- ARGV[1] = bucketCapacity (max tokens the bucket can hold)
-- ARGV[2] = refillRate (tokens added per second)
-- ARGV[3] = now (current timestamp in milliseconds, passed in from Java)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Step 1: fetch existing state, or use defaults if this is a new client
local bucket = redis.call("HMGET", key, "tokens", "last_refill")
local tokens = tonumber(bucket[1])
local lastRefill = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    lastRefill = now
end

-- Step 2: elapsed time since last refill, converted from ms to seconds
local elapsedSeconds = (now - lastRefill) / 1000

-- Step 3: how many tokens should've regenerated in that time
local tokensToAdd = elapsedSeconds * refillRate
tokens = math.min(capacity, tokens + tokensToAdd)

-- Step 4: update the refill timestamp to now
lastRefill = now

-- Step 5: decide allow or reject
local allowed = 0
if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

-- Step 6: persist state back to Redis, with a TTL so idle keys expire
redis.call("HMSET", key, "tokens", tokens, "last_refill", lastRefill)
redis.call("EXPIRE", key, 3600)

return allowed