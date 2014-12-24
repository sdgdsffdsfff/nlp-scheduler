package com.nlp.scheduler.controller.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Controller  公共组件,所有Controller必须继承至该对象
 * 
 * @author zhangwei
 *
 */
@SuppressWarnings("all")
public class BaseController {
	
	Logger LOG = LoggerFactory.getLogger(BaseController.class);
	
	/**
	 * 返回JSON数据
	 * @param res
	 * @param data 数据内容
	 * @param flag 成功或失败
	 * @param msg 提示信息
	 * @return
	 * @throws Exception
	 */
	public final ModelAndView toJson(HttpServletResponse res,Map data) throws Exception{
		PrintWriter out = res.getWriter();
		if(null ==data){
			data =new HashMap<String, Object>();
		}
		Map context = data;
		
		out.print(JSONObject.toJSONString(context,SerializerFeature.WriteDateUseDateFormat));
		
		out.flush();
		out.close();
		return null;
	}

	
}
