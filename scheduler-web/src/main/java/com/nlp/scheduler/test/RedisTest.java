package com.nlp.scheduler.test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {

	public static void main(String[] args) {
		try {
			JedisPool pool = new JedisPool(new JedisPoolConfig(), "127.0.0.1");
			Jedis jedis = pool.getResource();
			
			for(int i=4 ;i<105;i++){
				jedis.lpush("timer-queue", i+"");
				System.out.println("push "+i);
				Thread.sleep(1000*2);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
