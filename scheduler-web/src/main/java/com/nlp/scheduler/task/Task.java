package com.nlp.scheduler.task;

/**
 * 处理任务接口
 * @author zhangwei
 *
 */
public interface Task {

	/**
	 * 执行前置函数
	 * @throws Exception
	 */
	public void beforeExecute() throws Exception;
	
	/**
	 * 执行任务逻辑
	 * @return
	 * @throws Exception
	 */
	public void execute() throws Exception;
	
	/**
	 * 执行后置函数
	 * @throws Exception
	 */
	public void afterExecute() throws Exception;
	
	/**
	 * 设置任务的执行状态,1:成功,-1:执行失败
	 * @param status
	 */
	public void setTaskStatus(int status) ;
	
	/**
	 * 设置异常信息
	 * @param ex
	 */
	public void setException(Exception ex);
	
	/**
	 * 获取任务的表述
	 * @return
	 */
	public String desc() ;
}
