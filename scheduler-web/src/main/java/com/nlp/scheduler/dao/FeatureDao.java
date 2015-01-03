package com.nlp.scheduler.dao;

import com.nlp.scheduler.domain.SqlAdapter;

public class FeatureDao extends BaseDao{

	
	private String base = "com.nlp.scheduler.dao.FeatureDao.";
	
	/**
	 * 插入feature数据
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public boolean insertFeature(String sql) throws Exception{
		
		SqlAdapter adapter = new SqlAdapter();
		adapter.setSql(sql);
		
		return template.insert(getNameSpace("insertFeature"), adapter) > 0 ;
	}
	
	
	public boolean isExists(String sql) throws Exception{
		SqlAdapter adapter = new SqlAdapter();
		adapter.setSql(sql);
		Object obj = template.selectOne(getNameSpace("existsFeature"), adapter);
		if (null == obj){
			return false;
		}
		return (Integer)obj >0;
	}
	
	@Override
	protected String getNameSpace(String space) {
		return base + space;
	}
	
}
