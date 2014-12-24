package com.nlp.scheduler.controller.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.nlp.scheduler.controller.base.BaseController;

/**
 * 对外借口
 * @author wei
 *
 */
@Controller
@RequestMapping(value="/scheduler", method={RequestMethod.GET})
public class ApiController extends BaseController{

	Logger LOG = LoggerFactory.getLogger(ApiController.class);
	
	/**
	 * 实时出发扫描数据库的任务
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/triggerTask")
	public ModelAndView triggerTask(HttpServletRequest req, HttpServletResponse res) throws Exception{
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("status", "ok");
		data.put("msg", "实时扫描数据库任务触发成功");
		return toJson(res, data);
	}
	
	/**
	 * 动态加载配置
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/loadConfig")
	public ModelAndView loadConfig(HttpServletRequest req, HttpServletResponse res) throws Exception{
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("status", "ok");
		data.put("msg", "配置信息加载成功");
		return toJson(res, data);
	}
}
