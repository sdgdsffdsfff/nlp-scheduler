package com.nlp.scheduler.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nlp.scheduler.dao.HistoryDao;
import com.nlp.scheduler.domain.Config;

public class ParseGappTask implements Task{

	Logger log = LoggerFactory.getLogger(ParseGappTask.class);
	
	public int TASK_STATUS_SUCC = 1;//任务执行成功
	public int TASK_STATUS_FAIL = -1;//任务执行失败
	
	private int newId ;
	private int status;
	private Map<String, Map<String, Config>> config;
	private HistoryDao historyDao;
	private Exception ex;
	
	public ParseGappTask(int newId, HistoryDao historyDao, Map<String, Map<String, Config>> config){
		this.newId = newId;
		this.historyDao = historyDao;
		this.config = config;
	}
	
	@Override
	public void afterExecute() throws Exception {
		log.info("execute afterExecute");
	}
	
	@Override
	public void beforeExecute() throws Exception {
		log.info("execute beforeExecute");
		
	}
	
	@Override
	public String desc() {
		return null;
	}
	@Override
	public void execute() throws Exception {
		log.info("execute execute");
	}
	
	@Override
	public void setTaskStatus(int status) {
		this.status = status;
	}
	
}
