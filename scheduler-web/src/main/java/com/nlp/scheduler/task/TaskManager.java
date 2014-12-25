package com.nlp.scheduler.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nlp.scheduler.dao.ConfigDao;
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
	
	private ThreadPoolExecutor batchPool;//批量处理的线程池
	private ThreadPoolExecutor timePool; //实时处理的线程池
	
	@Resource
	private HistoryDao historyDao;
	
	@Resource
	private ConfigDao configDao;
	
	private ConfigManager configManager;
	/**
	 * 通过spring配置启动
	 */
	public void start() {
		run();
	}
	
	private void run (){
		try {
			//初始化配置信息
			this.configManager = ConfigManager.getInstance();
			List<Config> configs = configDao.queryAllConfig(gappVersion);
			this.configManager.setConfig(configs);
			
			int batchMaxSize = this.batchWorkSize * 2;
			int timerMaxSize = this.timerWorkSize * 2;
			
			LinkedBlockingQueue<Runnable> batchQueue = new LinkedBlockingQueue<Runnable>(batchMaxSize); 
			LinkedBlockingQueue<Runnable> timerQueue = new LinkedBlockingQueue<Runnable>(timerMaxSize);
			
			this.batchPool = new ThreadPoolExecutor(1, this.batchWorkSize, 5, TimeUnit.SECONDS, batchQueue);
			this.timePool = new ThreadPoolExecutor(1, this.timerWorkSize, 5, TimeUnit.SECONDS, timerQueue);
			
			Thread redisBatchThread = new Thread(new AddTaskThread(batchMaxSize, this, 1, this.historyDao, this.configManager.getConfig()));
			Thread redisTimerThread = new Thread(new AddTaskThread(timerMaxSize, this, 2, this.historyDao, this.configManager.getConfig()));
			
			redisBatchThread.start();
			redisTimerThread.start();
			
			log.info("taskManager starting success");
		} catch (Exception e) {
			log.error("taskManager starting fail",e);
			System.exit(0);
		}
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
			
			log.info("add task:["+task.desc()+"] to queue success,");
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
	Logger log = LoggerFactory.getLogger(AddTaskThread.class);
	
	public AddTaskThread(int maxSize, TaskManager manager, int flag ,HistoryDao historyDao, Map<String, Map<String, Config>> config) {
		this.maxSize = maxSize;
		this.manager = manager;
		this.flag = flag;
		this.historyDao = historyDao;
		this.config = config;
	}
	
	@Override
	public void run() {
		try {
			while(true){
				log.info("begin add task to queue");
				if (1 == flag){
					int batchSize = this.manager.getBatchQueueSize();
					log.info("add batchtask to queue, current queue size:"+batchSize+", maxSize:"+this.maxSize);
					//添加批处理数据到队列
					if (batchSize < this.maxSize) {
						for(int i = this.maxSize; i>batchSize ; i--){
							Task task = new ParseGappTask(1, this.historyDao, this.config);
							this.manager.addTask(task, flag);
						}
					}
				}else if ( 2 == flag){
					int timerSize = this.manager.getTimerQueueSize();
					log.info("add timertask to queue, current queue size:"+timerSize+", maxSize:"+this.maxSize);
					if (timerSize < this.maxSize){
						for(int i = this.maxSize; i>timerSize ; i--){
							Task task = new ParseGappTask(1, this.historyDao, this.config);
							this.manager.addTask(task, flag);
						}
					}
					
				}
				
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			log.error("add task fail.", e);
		}
		
	}
}
