package com.nlp.scheduler.dao;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;

/**
 * 数据访问层顶层依赖
 * 
 * @author cdzhangwei3
 *
 */
public abstract class BaseDao {

	@Resource(name="sqlTemplate")
	protected SqlSessionTemplate template;
	
	/**
	 * 获取数据库访问空间
	 * @param space
	 * @return
	 */
	protected abstract String getNameSpace(String space);
	
}
