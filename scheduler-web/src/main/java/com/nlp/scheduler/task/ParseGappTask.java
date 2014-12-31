package com.nlp.scheduler.task;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nlp.scheduler.dao.FeatureDao;
import com.nlp.scheduler.dao.HistoryDao;
import com.nlp.scheduler.domain.Config;
import com.nlp.scheduler.domain.Feature;
import com.nlp.scheduler.domain.History;

@SuppressWarnings("all")
public class ParseGappTask implements Task{

	Logger log = LoggerFactory.getLogger(ParseGappTask.class);
	
	public int TASK_STATUS_SUCC = 1;//任务执行成功
	public int TASK_STATUS_FAIL = -1;//任务执行失败
	
	private int newId ;
	private int status;
	private Map<String, Map<String, Config>> config;
	private HistoryDao historyDao;
	private Exception ex;
	private CorpusController annieController;
	private FeatureDao featureDao;
	private String gappVersion;//gapp版本号
	private String url;//文章访问地址
	private Date startTime;
	private List<List<Feature>> features;
	
	private boolean isExecute = false;//文章是否已经处理过了
	
	public ParseGappTask(int newId, HistoryDao historyDao, Map<String, Map<String, Config>> config, CorpusController annieController, FeatureDao featureDao, String url, String gappVersion){
		this.newId = newId;
		this.historyDao = historyDao;
		this.config = config;
		this.annieController = annieController;
		this.featureDao = featureDao;
		this.url = url+newId;
		this.gappVersion = gappVersion;
		this.features = new ArrayList<List<Feature>>();
	}
	
	@Override
	public void afterExecute() throws Exception {
		log.info("execute afterExecute");
		Date endTime = new Date();
		String executeMsg = (null == this.ex)?"success":this.ex.getLocalizedMessage();
		//首先插入历史记录
		History history = new History();
		history.setNewsId(this.newId);
		history.setGappVersion(this.gappVersion);
		history.setExecuteStartTime(this.startTime);
		history.setExecuteEndTime(endTime);
		history.setExecuteStatus(this.status);
		history.setExecuteMsg(executeMsg);
		
		try {
			historyDao.addHistory(history);
			log.info("insert execute history success");
		} catch (Exception e) {
			log.error("insert execute history fail.", e);
			return ;
		}
		
		//开始插入标签数据
		if(null != this.features && this.features.size() >0 ){
			
			for (List<Feature> features : this.features){
				
				
				
				Map<String, String> tablesMapp = tables(features);
				Iterator<String> tables = tablesMapp.keySet().iterator();
				while (tables.hasNext()){
					String table = tables.next();
					StringBuffer InsertSql = new StringBuffer();
					InsertSql.append("insert into ").append(table);
					InsertSql.append("(type,news_id,nlp_history_id,feature,");
					
					StringBuffer valuesSql = new StringBuffer("values(");
					
					for(int i=0;i<features.size();i++){
						Feature feature = features.get(i);
						if (!table.equals(feature.getTable())){
							continue;
						}
						
						if (i ==0 ){//第一个
							InsertSql.append(feature.getColumn()).append(",");
							valuesSql.append("'"+feature.getType()+"',").append(feature.getNewsId()).append(",").append(feature.getHistoryId()).append(",").append("'"+feature.getFeature()+"',").append("'"+feature.getVal()+"',");
						}else if (i == features.size()-1){//最后一个
							InsertSql.append(feature.getColumn());
							valuesSql.append("'"+feature.getVal()+"'");
						}else{
							InsertSql.append(feature.getColumn()).append(",");
							valuesSql.append("'"+feature.getVal()+"',");
						}
					}
					InsertSql.append(")");
					valuesSql.append(")");
					StringBuffer sql = new StringBuffer();
					sql.append(InsertSql.toString()).append(valuesSql.toString());
					try {
						this.featureDao.insertFeature(sql.toString());
					} catch (Exception e) {
						log.error("insert feature sql:"+sql.toString()+", fail:",e);
					}
					
					
				}
			}
		}
		
		
	}
	
	/**
	 * 判断这些features需要存储的表名称
	 * @param fratures
	 * @return
	 */
	private Map<String, String> tables(List<Feature> fratures) {
		Map<String, String> tables = new HashMap<String, String>();
		if (null != features && features.size()>0){
			for(Feature feature:fratures){
				if(!tables.containsKey(feature.getTable())){
					tables.put(feature.getTable(), feature.getTable());
				}
			}
		}
		return tables;
	}
	
	@Override
	public void beforeExecute() throws Exception {
		log.info("execute beforeExecute");
		this.isExecute = this.historyDao.isExists(this.newId, this.gappVersion);
		this.startTime = new Date();
	}
	
	@Override
	public String desc() {
		StringBuffer desc = new StringBuffer();
		desc.append("newId:").append(this.newId);
		desc.append("gappversion:").append(this.gappVersion);
		desc.append("new_request_url:").append(this.url);
		return desc.toString();
	}
	
	
	@Override
	public void execute() throws Exception {
		log.info("begin execute");
		Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
		URL u = new URL(this.url);
		FeatureMap params = Factory.newFeatureMap();
		params.put("sourceUrl", u);
		params.put("preserveOriginalContent", new Boolean(true));
		params.put("collectRepositioningInfo", new Boolean(true));
		Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl", params);
		corpus.add(doc);
		
		//调用nlp
		this.annieController.setCorpus(corpus);
		this.annieController.execute();
	    
	    //解析数据
		Iterator iter = corpus.iterator();
		if (null == iter){
			return;
		}
		while(iter.hasNext()){
			Document dom = (Document) iter.next();
	    	AnnotationSet annotationSet =  dom.getAnnotations();
	    	
	    	Iterator<Annotation> it = annotationSet.iterator();
	    	if (null == it){
	    		log.warn("According to the configuration does not need to output");
    			continue;
	    	}
	    	//遍历所有type进行处理
	    	while(it.hasNext()){
	    		Annotation annotation = it.next();
	    		if (null == annotation){
	    			continue;
	    		}
	    		
	    		String type = annotation.getType();
	    		//判断这个type是否需要输出
	    		if (!this.config.containsKey(type)){
	    			log.warn("type:"+type+", According to the configuration does not need to output");
	    			continue;
	    		}
	    		Map<String, Config> featureConfigs = this.config.get(type);
	    		FeatureMap featureMap = annotation.getFeatures();
	    		if (null == featureMap){
	    			log.warn("type:"+type+", features is empty");
	    			continue;
	    		}
	    		
	    		//遍历所有的features进行输出
	    		Iterator<Object> featureKeys = featureMap.keySet().iterator();
	    		List<Feature> fs = new ArrayList<Feature>();
	    		while(featureKeys.hasNext()){
	    			Object keyObj = featureKeys.next();
	    			Object valObj = featureMap.get(keyObj);
	    			if (null == keyObj){
	    				log.warn("type:"+type+", key is null");
	    				continue;
	    			}
	    			
	    			String key = keyObj.toString();
	    			String val = (null ==valObj)?"":valObj.toString();
	    			//判断key是否需要输出
	    			if(!featureConfigs.containsKey(key)){
	    				log.warn("type:"+type+",feature:"+key+", According to the configuration does not need to output");
	    				continue;
	    			}
	    			Config featureConf = featureConfigs.get(key);
	    			
	    			Feature featureDomain = new Feature();
	    			featureDomain.setType(type);
	    			featureDomain.setFeature(key);
	    			featureDomain.setVal(val);
	    			featureDomain.setTable(featureConf.getTableName());
	    			featureDomain.setColumn(featureConf.getColumnName());
	    			featureDomain.setNewsId(this.newId);
	    			fs.add(featureDomain);
	    		}
	    		this.features.add(fs);
	    	}
		}
		
	}
	
	@Override
	public void setTaskStatus(int status) {
		this.status = status;
	}

	@Override
	public void setException(Exception ex) {
		this.ex = ex;
	}
}
