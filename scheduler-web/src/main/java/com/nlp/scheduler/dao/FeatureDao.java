package com.nlp.scheduler.dao;

public class FeatureDao extends BaseDao{

	
	private String base = "com.nlp.scheduler.dao.FeatureDao.";
	
	/**
	 * 插入feature数据
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public boolean insertFeature(String sql) throws Exception{
		
		return template.insert(getNameSpace("insertFeature"), sql) > 0 ;
	}
	
	@Override
	protected String getNameSpace(String space) {
		return base + space;
	}
	
}
