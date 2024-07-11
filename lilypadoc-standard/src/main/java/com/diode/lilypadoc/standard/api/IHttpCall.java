package com.diode.lilypadoc.standard.api;

import com.diode.lilypadoc.standard.common.Result;
import com.diode.lilypadoc.standard.domain.http.HttpCallContext;

import java.util.Map;

/**
 * 如果你想要让插件接收到前端页面的信息，请让插件实现该类
 * if you want to make plugin can receive the request from the web, make sure your plugin class implements this class.
 */
public interface IHttpCall {

    /**
     * @param paramMap 前端参数 param from web
     * @param httpCallContext 上下文 context from application
     * @return
     */
    Result<Map<String, String>> httpCall(Map<String, String> paramMap, HttpCallContext httpCallContext);
}
