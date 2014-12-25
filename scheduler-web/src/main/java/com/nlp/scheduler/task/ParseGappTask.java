package com.nlp.scheduler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseGappTask implements Task{

	Logger log = LoggerFactory.getLogger(ParseGappTask.class);
	
	public int TASK_STATUS_SUCC = 1;//任务执行成功
	public int TASK_STATUS_FAIL = -1;//任务执行失败
	
	private int newId ;
	private int status;
	
	public ParseGappTask(int newId){
		this.newId = newId;
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
