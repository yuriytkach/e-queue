package com.yuriytkach.demo.e_queue;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("User")
public record User(@Id String id, String name) { }
