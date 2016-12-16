package org.virtus.sense.store;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisStore extends DeviceStore {

	private static final String LOCALHOST = "localhost";
	private static final String REDIS_MAP_KEY = "VirtusSenseModbusStore";
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static JedisPool pool;
	
	public RedisStore() {
		this.open(LOCALHOST);
	}
	
	public RedisStore(String redisURI) {
		this.open(redisURI);
	}
	
	private void open(String redisURI) {
		
		// force close of previous pool
		if(pool != null && !pool.isClosed()) {
			try {
				this.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		pool = new JedisPool(new JedisPoolConfig(), redisURI);
		cachedDevices = new ConcurrentHashMap<>();
		
		loadStore();
	}

	private void loadStore() {
		try (Jedis jedis = pool.getResource()) {
			if(jedis.exists(REDIS_MAP_KEY)) {
				Map<String, String> map = jedis.hgetAll(REDIS_MAP_KEY);
				
				this.cachedDevices = map.entrySet()
					.stream()
					.collect(Collectors.toMap(
						e -> Integer.parseInt(e.getKey()), 
						e -> {
							try {
								return (StoredDevice) mapper.readValue(e.getValue(), StoredDevice.class);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								return null;
							}
						}));
			}
		}
	}

	@Override
	public Collection<StoredDevice> getCachedDevices() {
		return cachedDevices.values();
	}

	@Override
	public void cacheDevice(StoredDevice device) {
		try (Jedis jedis = pool.getResource()) {
			// save to Redis			
			jedis.hset(
					REDIS_MAP_KEY, 
					Integer.toString(device.networkAddress), 
					mapper.writeValueAsString(device));
			
			// replicate in local Map
			this.cachedDevices.put(device.networkAddress, device);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void close() throws Exception {
		pool.close();
		pool.destroy();
	}

}
