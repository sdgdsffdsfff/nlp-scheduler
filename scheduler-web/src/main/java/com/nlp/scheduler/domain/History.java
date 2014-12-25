package com.nlp.scheduler.domain;

import java.util.Date;

/**
 * NLP 调度系统处理的历史数据
 * 
 * @author zhangwei
 *
 */
public class History {

	private int id;
	private int newsId;//文章id
	private String gappVersion;//处理的gapp版本号
	private Date executeStartTime;//处理开始时间
	private Date executeEndTime;//处理结束时间
	private int  executeStatus;//处理状态
	private String executeMsg;//处理描述
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNewsId() {
		return newsId;
	}
	public void setNewsId(int newsId) {
		this.newsId = newsId;
	}
	public String getGappVersion() {
		return gappVersion;
	}
	public void setGappVersion(String gappVersion) {
		this.gappVersion = gappVersion;
	}
	public Date getExecuteStartTime() {
		return executeStartTime;
	}
	public void setExecuteStartTime(Date executeStartTime) {
		this.executeStartTime = executeStartTime;
	}
	public Date getExecuteEndTime() {
		return executeEndTime;
	}
	public void setExecuteEndTime(Date executeEndTime) {
		this.executeEndTime = executeEndTime;
	}
	public int getExecuteStatus() {
		return executeStatus;
	}
	public void setExecuteStatus(int executeStatus) {
		this.executeStatus = executeStatus;
	}
	public String getExecuteMsg() {
		return executeMsg;
	}
	public void setExecuteMsg(String executeMsg) {
		this.executeMsg = executeMsg;
	}
	
}
