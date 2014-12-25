package com.nlp.scheduler.dao;

import java.util.List;

import com.nlp.scheduler.domain.Config;

public class ConfigDao extends BaseDao{

	private String base =	"com.nlp.scheduler.dao.ConfigDao.";
	
	/**
	 * 查询feature 存储规则的配置文件
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public List<Config> queryAllConfig(String gappVersion) throws Exception{
	
		return template.selectList(getNameSpace("selectConfig"), gappVersion);
	}
	
	
	@Override
	protected String getNameSpace(String space) {
		return base + space;
	}
	
}
