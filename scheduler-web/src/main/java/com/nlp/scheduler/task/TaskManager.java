package com.nlp.scheduler.task;

import gate.CorpusController;
import gate.Gate;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.nlp.scheduler.dao.ConfigDao;
import com.nlp.scheduler.dao.FeatureDao;
import com.nlp.scheduler.dao.HistoryDao;
import com.nlp.scheduler.domain.Config;

/**
 * 任务管理对象
 * 
 * 负责创建线程池,并且调度任务
 * 
 * @author zhangwei
 *
 */
@SuppressWarnings("all")
public final class TaskManager {

	Logger log = LoggerFactory.getLogger(TaskManager.class);
	
	@Value(value="${batch_work_size}")
	private int batchWorkSize;//批处理最大处理线程数
	@Value(value="${timer_work_size}")
	private int timerWorkSize;//实时处理最大处理线程数
	@Value(value="${gapp_version}")
	private String gappVersion;//gapp 版本号
	@Value(value="${gapp_name}")
	private String gappName;//gapp名称
	@Value(value="${gate.home}")
	private String gateHome;//gate 安装目录
	@Value(value="${gate.plugins.home}")
	private String gatePlugin;//gate 插件
	@Value(value="${gate.site.conﬁg}")
	private String gateSiteConf;//gate配置
	
	@Value(value="${news_request_url}")
	private String url;
	@Value(value="${redis.batch.key}")
	private String batchRedisKey;
	@Value(value="${redis.timer.key}")
	private String timerRedisKey;
	
	
	private ThreadPoolExecutor batchPool;//批量处理的线程池
	private ThreadPoolExecutor timePool; //实时处理的线程池
	
	@Resource
	private HistoryDao historyDao;
	
	@Resource
	private ConfigDao configDao;
	
	@Resource
	private FeatureDao featureDao;
	
	@Resource
	private StringRedisTemplate redis;//redis 服务
	
	private ConfigManager configManager;
	
	private CorpusController annineController;//gate controller
	
	/**
	 * 通过spring配置启动
	 */
	public void start() {
		run();
	}
	
	private void run (){
		try {
			
			//初始化 gate
			initGate();
			//初始化配置信息
			this.configManager = ConfigManager.getInstance();
			List<Config> configs = configDao.queryAllConfig(gappVersion);
			this.configManager.setConfig(configs);
			
			int batchMaxSize = this.batchWorkSize * 5000;
			int timerMaxSize = this.timerWorkSize * 5000;
			
			LinkedBlockingQueue<Runnable> batchQueue = new LinkedBlockingQueue<Runnable>(batchMaxSize); 
			LinkedBlockingQueue<Runnable> timerQueue = new LinkedBlockingQueue<Runnable>(timerMaxSize);
			
			this.batchPool = new ThreadPoolExecutor(1, this.batchWorkSize, 5, TimeUnit.SECONDS, batchQueue);
			this.timePool = new ThreadPoolExecutor(1, this.timerWorkSize, 5, TimeUnit.SECONDS, timerQueue);
			
			Thread redisBatchThread = new Thread(new AddTaskThread(batchMaxSize, this, 1, this.historyDao, this.configManager.getConfig(), this.annineController, this.featureDao, this.url, this.gappVersion, this.redis, this.batchRedisKey));
			Thread redisTimerThread = new Thread(new AddTaskThread(timerMaxSize, this, 2, this.historyDao, this.configManager.getConfig(), this.annineController, this.featureDao, this.url, this.gappVersion, this.redis, this.timerRedisKey));
			
			redisBatchThread.start();
			redisTimerThread.start();
			
			log.info("taskManager starting success");
		} catch (Exception e) {
			log.error("taskManager starting fail",e);
			System.exit(0);
		}
	}
	
	/**
	 * 初始化gate
	 * @throws Exception
	 */
	private void initGate() throws Exception{
		Properties properties = System.getProperties();
		properties.setProperty("gate.home", this.gateHome);
		properties.setProperty("gate.plugins.home", this.gatePlugin);
		properties.setProperty("gate.site.conﬁg", this.gateSiteConf);
		log.info("set gate properties gate.home"+this.gateHome+", gate.plugins.home:"+this.gatePlugin+", gate.site.config:"+this.gateSiteConf+", gapp_version:"+this.gappVersion);
		Gate.init();
		
		File pluginsHome = Gate.getPluginsHome();
	    File anniePlugin = new File(pluginsHome, "ANNIE");
	    File annieGapp = new File(anniePlugin, this.gappName);
	    this.annineController = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
	    
	    log.info("gate init success");
	    
	}
	
	/**
	 * 添加待处理的任务到队列
	 * @param task
	 * @param flag 1:批处理任务, 2:实时处理任务
	 * @throws Exception
	 */
	protected boolean addTask(Task task, int flag){
		try {
			
			if(1 == flag){
				if(null == this.batchPool){
					log.error("add task to batchPool fail, batchPool is not init");
					return false;
				}
			}else if (2 == flag){
				if(null == this.timePool){
					log.error("add task to timerPool fail, timerPool is not init");
					return false;
				}
			}
			
			ExecuteTask commd = new ExecuteTask(task);
			
			if (1 == flag){
				this.batchPool.execute(commd);
			}else if (2 == flag){
				this.timePool.execute(commd);
			}
			
			log.debug("add task:["+task.desc()+"] to queue success,");
		} catch (Exception e) {
			log.error("addTask to queue fail.", e);
			return false;
		}
		
		return true;
	}
	
	protected int getBatchQueueSize(){
		return this.batchPool.getQueue().size();
	}
	
	protected int getTimerQueueSize(){
		return this.batchPool.getQueue().size();
	}
}

//任务执行对象
final class ExecuteTask implements Runnable{
	
	private Task task ;//文章ID
	Logger log = LoggerFactory.getLogger(ExecuteTask.class);
	
	ExecuteTask(Task task) {
		this.task = task;
	}
	
	@Override
	public void run() {
		
		try {
			
			if (null == this.task){
				log.error("execute task fail, task is null");
				return;
			}
			
			//执行前置任务
			try {
				this.task.beforeExecute();
			} catch (Exception e) {
				log.error(this.task.desc()+" beforeExecute fail, ", e);
			}
			
			//执行任务
			try {
				this.task.execute();
				this.task.setTaskStatus(1);
			} catch (Exception e) {
				this.task.setException(e);
				this.task.setTaskStatus(-1);
			}
			
			//执行后置任务
			try {
				this.task.afterExecute();
			} catch (Exception e) {
				log.error(this.task.desc()+" afterExecute fail.", e);
			}
		} catch (Exception e) {
			log.error("execute task fail.", e);
		}
	}
} 

final class AddTaskThread implements Runnable {
	
	private int maxSize;
	private TaskManager manager;
	private int flag;
	private Map<String, Map<String, Config>> config;
	private HistoryDao historyDao;
	private CorpusController annieController;
	private FeatureDao featureDao;
	private String url;
	private String gappVersion;
	private StringRedisTemplate redis;
	private String redisKey;
	private int count =5;
	
	Logger log = LoggerFactory.getLogger(AddTaskThread.class);
	
	public AddTaskThread(int maxSize, TaskManager manager, int flag ,HistoryDao historyDao, Map<String, Map<String, Config>> config,  CorpusController annieController, FeatureDao featureDao, String url, String gappVersion, StringRedisTemplate redis, String redisKey) {
		this.maxSize = maxSize;
		this.manager = manager;
		this.flag = flag;
		this.historyDao = historyDao;
		this.config = config;
		this.annieController = annieController;
		this.featureDao = featureDao;
		this.url = url;
		this.gappVersion = gappVersion;
		this.redis = redis;
		this.redisKey = redisKey;
		this.count = (this.maxSize/5000)*5;
		if (this.count <5){
			this.count = 5;
		}
	}
	
	@Override
	public void run() {
		while(true){
			try {
				log.info("begin add task to queue");
				if (1 == flag){
					int batchSize = this.manager.getBatchQueueSize();
					log.debug("add batchtask to queue, current queue size:"+batchSize+", maxSize:"+this.maxSize);
					//添加批处理数据到队列
					if (batchSize < this.maxSize) {
						for(int i = 0; i<this.count ; i++){
							String pop_newId = this.redis.opsForList().rightPop(this.redisKey);
							if (null == pop_newId || "".equals(pop_newId)){
								break;
							}
							try {
								int newId = Integer.parseInt(pop_newId);
								Task task = new ParseGappTask(newId, this.historyDao, this.config, this.annieController, this.featureDao, this.url, this.gappVersion);
								this.manager.addTask(task, flag);
							} catch (Exception e) {
								log.error("redis pop from key:"+this.redisKey+", result:"+pop_newId+" not integer");
							}
							
						}
					}
				}else if ( 2 == flag){
					int timerSize = this.manager.getTimerQueueSize();
					log.debug("add timertask to queue, current queue size:"+timerSize+", maxSize:"+this.maxSize);
					if (timerSize < this.maxSize){
						for(int i = 0; i<this.count ; i++){
							String pop_newId = this.redis.opsForList().rightPop(this.redisKey);
							if (null == pop_newId || "".equals(pop_newId)){
								break;
							}
							try {
								int newId = Integer.parseInt(pop_newId);
								Task task = new ParseGappTask(newId, this.historyDao, this.config, this.annieController, this.featureDao, this.url, this.gappVersion);
								this.manager.addTask(task, flag);
							} catch (Exception e) {
								log.error("redis pop from key:"+this.redisKey+", result:"+pop_newId+" not integer");
							}
							
						}
					}
					
				}
				
				Thread.sleep(5000);
			} catch (Exception e) {
				log.error("redis pop key:"+this.redisKey+" and add task fail.", e);
			}
		}
	}
}
