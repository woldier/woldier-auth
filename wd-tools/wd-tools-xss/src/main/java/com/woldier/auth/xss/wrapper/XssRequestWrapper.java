package com.woldier.auth.xss.wrapper;

import com.woldier.auth.xss.utils.XssUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;
import java.util.Map;

import static com.woldier.auth.xss.utils.XssUtils.xssClean;
/**
 * HttpServletRequest的包裹类 用于处理表单
 * 在filter chain中用XssRequestWrapper 就可以对相应的参数进行校验
 *
 * 过滤器放行时需要调用filterChain.doFilter()方法，此方法需要传入请求Request对象，
 * 此时我们可以将当前的request对象进行包装，而XssRequestWrapper就是Request对象的包装类，
 * 在过滤器放行时会自动调用包装类的getParameterValues方法，我们可以在包装类的getParameterValues方法中进行统一的请求参数过滤清理。
 */

/**
 * 跨站攻击请求包装器
 *
 */
@Slf4j
public class XssRequestWrapper extends HttpServletRequestWrapper {
    private List<String> ignoreParamValueList;

    public XssRequestWrapper(HttpServletRequest request, List<String> ignoreParamValueList) {
        super(request);
        this.ignoreParamValueList = ignoreParamValueList;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> requestMap = super.getParameterMap();
        for (Map.Entry<String, String[]> me : requestMap.entrySet()) {
            log.debug(me.getKey() + ":");
            String[] values = me.getValue();
            for (int i = 0; i < values.length; i++) {
                log.debug(values[i]);
                values[i] = XssUtils.xssClean(values[i], this.ignoreParamValueList);
            }
        }
        return requestMap;
    }

    /**
     * 重写这个方法
     * filter chain 中 使用了XssRequestWrapper的包装类会自动调用该方法
     * 以达到过滤的目的
     * @param paramString
     * @return
     */
    @Override
    public String[] getParameterValues(String paramString) {
        String[] arrayOfString1 = super.getParameterValues(paramString);
        if (arrayOfString1 == null) {//若为空直接返回
            return null;
        }
        int i = arrayOfString1.length;
        /*创建一个新数组来封装*/
        String[] arrayOfString2 = new String[i];
        for (int j = 0; j < i; j++) {
            arrayOfString2[j] = XssUtils.xssClean(arrayOfString1[j], this.ignoreParamValueList);
        }
        return arrayOfString2;
    }


    @Override
    public String getParameter(String paramString) {
        String str = super.getParameter(paramString);
        if (str == null) {
            return null;
        }
        return XssUtils.xssClean(str, this.ignoreParamValueList);
    }

    /**
     * 请求头校验
     * @param paramString
     * @return
     */
    @Override
    public String getHeader(String paramString) {
        String str = super.getHeader(paramString);
        if (str == null) {
            return null;
        }
        return XssUtils.xssClean(str, this.ignoreParamValueList);
    }
}
