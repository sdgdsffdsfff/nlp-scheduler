package com.nlp.scheduler.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.nlp.scheduler.controller.base.BaseController;
import com.nlp.scheduler.dao.ConfigDao;
import com.nlp.scheduler.domain.Config;
import com.nlp.scheduler.task.ConfigManager;

/**
 * 对外借口
 * @author wei
 *
 */
@Controller
@RequestMapping(value="/scheduler", method={RequestMethod.GET})
public class ApiController extends BaseController{

	Logger LOG = LoggerFactory.getLogger(ApiController.class);
	
	@Resource
	private ConfigDao configDao;
	
	@Value(value="${gapp_version}")
	private String gappVersion;//gapp 版本号
	
	
	
	/**
	 * 动态加载配置
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/loadConfig")
	public ModelAndView loadConfig(HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		LOG.info("begin load config");
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			List<Config> configs = configDao.queryAllConfig(this.gappVersion);
			ConfigManager.getInstance().setConfig(configs);
			LOG.info("load config success");
			data.put("status", "ok");
			data.put("msg", "load config success");
		} catch (Exception e) {
			data.put("status", "fail");
			data.put("msg", "load config fail,"+e.getLocalizedMessage());
		}
		return toJson(res, data);
	}
}
