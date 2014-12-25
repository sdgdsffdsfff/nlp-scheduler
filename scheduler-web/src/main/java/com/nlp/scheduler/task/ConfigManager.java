package com.nlp.scheduler.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.nlp.scheduler.domain.Config;

public class ConfigManager {

	private static ConfigManager instance;
	private ConfigManager(){}
	private Map<String, Map<String, Config>> config;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Logger log = LoggerFactory.getLogger(ConfigManager.class);
	
	public synchronized static ConfigManager getInstance(){
		if (null == instance){
			instance = new ConfigManager();
		}
		return instance;
	}
	
	/**
	 * 设置config信息
	 * @param configs
	 */
	public void setConfig(List<Config> configs) {
		this.log.info("begin init feature configs");
		try {
			this.lock.writeLock().lock();
			if (null == this.config){
				this.config = new HashMap<String, Map<String,Config>>();
			}else{
				this.config = null;
				this.config = new HashMap<String, Map<String,Config>>();
			}
			
			if (null != configs && configs.size()>0){
				
				for(Config conf : configs){
					String type = conf.getType();
					String feature = conf.getFeature();
					
					Map<String, Config> features = null;
					if (this.config.containsKey(type)){
						features = this.config.get(type);
					}else{
						features = new HashMap<String, Config>();
					}
					this.log.info("init config info:"+JSONObject.toJSONString(conf));
					features.put(feature, conf);
					this.config.put(type, features);
				}
				
			}
		} catch (Exception e) {
			this.log.error("setConfig fail.", e);
		}finally{
			this.lock.writeLock().unlock();
		}
		
	}
	
	public Map<String, Map<String, Config>> getConfig() {
		try {
			this.lock.readLock().lock();
			return this.config;
		} finally{
			this.lock.readLock().unlock();
		}
	}
}
