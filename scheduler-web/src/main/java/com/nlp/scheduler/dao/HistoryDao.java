package com.nlp.scheduler.dao;

import java.util.HashMap;
import java.util.Map;

import com.nlp.scheduler.domain.History;

public class HistoryDao extends BaseDao{

	private String base = "com.nlp.scheduler.dao.HistoryDao.";
	
	/**
	 * 通过gapp和新闻id判断该新闻是否处理过
	 * @param newsId
	 * @param gappVersion
	 * @return
	 * @throws Exception
	 */
	public boolean isExists(int newsId, String gappVersion) throws Exception{
	
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("newId", newsId);
		param.put("gappVersion", gappVersion);
		
		Object obj = template.selectOne(getNameSpace("query"), param);
		if (null == obj){
			return false;
		}
		return true;
	}
	
	/**
	 * 保存历史数据
	 * @param history
	 * @return
	 * @throws Exception
	 */
	public boolean addHistory(History history) throws Exception{
		
		return template.insert(getNameSpace("addHistory"), history)>0;
	}
	
	@Override
	protected String getNameSpace(String space) {
		return base + space;
	}
	
}
