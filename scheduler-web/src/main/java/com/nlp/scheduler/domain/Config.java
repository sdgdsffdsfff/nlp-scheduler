package com.nlp.scheduler.domain;

/**
 * 配置信息
 * @author zhangwei
 *
 */
public class Config {

	private String type;//需要解析的type
	private String feature; //需要解析的feature
	private String tableName;//feature 存储的表名称
	private String columnName;//feature存储的列名称
	private String gappVersion;//gapp版本号
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
}
