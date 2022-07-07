package com.woldier.auth.log.aspect;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSONObject;
import com.woldier.auth.base.R;
import com.woldier.auth.context.BaseContextHandler;
import com.woldier.auth.log.entity.OptLogDTO;
import com.woldier.auth.log.event.SysLogEvent;
import com.woldier.auth.log.util.LogUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 操作日志使用spring event异步入库
 *
 */
@Slf4j
@Aspect
public class SysLogAspect {

    /**
     * 事件发布是由ApplicationContext对象管控的，我们发布事件前需要注入ApplicationContext对象调用publishEvent方法完成事件发布
     **/
    @Autowired
    private ApplicationContext applicationContext;

    /*线程实体*/
    private static final ThreadLocal<OptLogDTO> THREAD_LOCAL = new ThreadLocal<>();

    /***
     * 定义controller切入点拦截规则，拦截SysLog注解的方法
     */
    @Pointcut("@annotation(com.woldier.auth.log.annotation.SysLog)")
    public void sysLogAspect() {

    }

    /**
     * 通过THREAD_LOCAL获取OptLogDTO实体
     * @return
     */
    private OptLogDTO get() {
        /*THREAD_LOCAL中有则拿THREAD_LOCAL线程中的OptLogDTO实体*/
        OptLogDTO sysLog = THREAD_LOCAL.get();
        if (sysLog == null) {
            /*THREAD_LOCAL中没有则拿创建OptLogDTO实体*/
            return new OptLogDTO();
        }
        return sysLog;
    }

    /**
     * 前置通知
     * @param joinPoint
     * @throws Throwable
     */
    @Before(value = "sysLogAspect()") //指定为哪个切点的前置
    public void recordLog(JoinPoint joinPoint) throws Throwable {
        tryCatch((val) -> {
            // 开始时间
            /*获取线程中的OptLogDTO实体，没有则创建*/
            OptLogDTO sysLog = get();
            /*通过wd-tools-common中的类获取一些信息*/
            sysLog.setCreateUser(BaseContextHandler.getUserId());
            sysLog.setUserName(BaseContextHandler.getName());

            /*controller 描述信息*/
            String controllerDescription = "";

            /*此信息来自于swagger2- 若controller上加了@Api则可以获得其中的描述信息*/
            Api api = joinPoint.getTarget().getClass().getAnnotation(Api.class);
            if (api != null) {
                String[] tags = api.tags();
                if (tags != null && tags.length > 0) {
                    controllerDescription = tags[0];
                }
            }
            /*切点信息获取的封装*/
            String controllerMethodDescription = LogUtil.getControllerMethodDescription(joinPoint);

            /*获取到的 controllerDescription 为null时 只写入controllerMethodDescription*/
            if (StrUtil.isEmpty(controllerDescription)) {
                sysLog.setDescription(controllerMethodDescription);
            } else {
                /*controllerDescription 不为null时 ，将其也写入*/
                sysLog.setDescription(controllerDescription + "-" + controllerMethodDescription);
            }

            // 存储类名
            sysLog.setClassPath(joinPoint.getTarget().getClass().getName());
            //获取执行的方法名
            sysLog.setActionMethod(joinPoint.getSignature().getName());


            // 参数
            Object[] args = joinPoint.getArgs();


            String strArgs = "";
            /**
             * Objects.requireNonNull(obj)检查指定的对象引用是否为空。该方法主要用于在方法和构造函数中进行参数验证，如下所示:
             *  public Foo(Bar bar) {
             *            this.bar = Objects.requireNonNull(bar);
             *        }
             *
             *从spring中的RequestContextHolder中得到与当前线程绑定的请求参数（若为空则抛出异常） 并且强转为ServletRequestAttributes
             */
            HttpServletRequest request = (
                    (ServletRequestAttributes) Objects
                            .requireNonNull(RequestContextHolder.getRequestAttributes())
                    ).getRequest();
            try {
                /*如果传递的参数不是multipart/form-data 则通过json对象转换器转换为字符串*/
                if (!request.getContentType().contains("multipart/form-data")) {
                    strArgs = JSONObject.toJSONString(args);
                }
            } catch (Exception e) {
                try {
                    /*如果json解析失败的话就通过Arrays.toString解决*/
                    strArgs = Arrays.toString(args);
                } catch (Exception ex) {
                    log.warn("解析参数异常", ex);
                }
            }
            /*截取字符串的最大长度为65535*/
            sysLog.setParams(getText(strArgs));

            if (request != null) {
                /*获取ip*/
                sysLog.setRequestIp(ServletUtil.getClientIP(request));
                sysLog.setRequestUri(URLUtil.getPath(request.getRequestURI()));
                sysLog.setHttpMethod(request.getMethod());
                /*获取请求头agent中的数据*/
                sysLog.setUa(StrUtil.sub(request.getHeader("user-agent"), 0, 500));
            }
            sysLog.setStartTime(LocalDateTime.now());
            /*在前置通知处理完后吧线程id保存 ，用于在后置通知时进行获取该线程对象，免于后置通知时重新创建该对象*/
            THREAD_LOCAL.set(sysLog);
        });
    }


    private void tryCatch(Consumer<String> consumer) {
        try {
            consumer.accept("");
        } catch (Exception e) {
            log.warn("记录操作日志异常", e);
            THREAD_LOCAL.remove();
        }
    }

    /**
     * 返回通知（后置通知）
     *
     * @param ret
     * @throws Throwable
     */
    @AfterReturning(returning = "ret", pointcut = "sysLogAspect()")
    public void doAfterReturning(Object ret) {
        tryCatch((aaa) -> {
            R r = Convert.convert(R.class, ret);
            /*从线程中获得OptLogDTO*/
            OptLogDTO sysLog = get();
            if (r == null) {
                sysLog.setType("OPT");
            } else {
                if (r.getIsSuccess()) {
                    sysLog.setType("OPT");
                } else {
                    sysLog.setType("EX");
                    sysLog.setExDetail(r.getMsg());
                }
                sysLog.setResult(getText(r.toString()));
            }

            /*发布log*/
            //另一个线程中SysLogListener
            publishEvent(sysLog);
        });

    }


    /**
     *
     * 事件的发布
     * @param sysLog
     */
    private void publishEvent(OptLogDTO sysLog) {
        sysLog.setFinishTime(LocalDateTime.now());
        sysLog.setConsumingTime(sysLog.getStartTime().until(sysLog.getFinishTime(), ChronoUnit.MILLIS));

        applicationContext.publishEvent(new SysLogEvent(sysLog));

        /**
         * 移除此线程局部变量的当前线程值。
         * 如果这个线程局部变量随后被当前线程读取，它的值将通过调用它的initialValue方法重新初始化，除非它的值在此期间是由当前线程设置的。
         * 这可能导致在当前线程中多次调用initialValue方法。
         */
        THREAD_LOCAL.remove();
    }

    /**
     * 异常通知
     *
     * @param e
     */
    @AfterThrowing(pointcut = "sysLogAspect()", throwing = "e")
    public void doAfterThrowable(Throwable e) {
        tryCatch((aaa) -> {
            OptLogDTO sysLog = get();
            sysLog.setType("EX");

            // 异常对象
            sysLog.setExDetail(LogUtil.getStackTrace(e));
            // 异常信息
            sysLog.setExDesc(e.getMessage());

            publishEvent(sysLog);
        });
    }

    /**
     * 截取指定长度的字符串
     *
     * @param val
     * @return
     */
    private String getText(String val) {
        return StrUtil.sub(val, 0, 65535);
    }

//    @Around("@annotation(sLog)")
//    @SneakyThrows
//    public Object around(ProceedingJoinPoint point, SysLog sLog) {
//        log.info("当前线程id={}", Thread.currentThread().getId());
//
//        String strClassName = point.getTarget().getClass().getName();
//        String strMethodName = point.getSignature().getName();
//
//        log.info("[类名]:{},[方法]:{}", strClassName, strMethodName);
//        Log sysLog = Log.builder().build();
//
//        // 开始时间
//        Long startTime = Instant.now().toEpochMilli();
//        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
//        BaseContextHandler.getAccount();
//        sysLog.setCreateUser(BaseContextHandler.getUserId());
//        sysLog.setRequestIp(ServletUtil.getClientIP(request));
//        sysLog.setUserName(BaseContextHandler.getNickName());
//        sysLog.setDescription(LogUtil.getControllerMethodDescription(point));
//
//        // 类名
//        sysLog.setClassPath(point.getTarget().getClass().getName());
//        //获取执行的方法名
//        sysLog.setActionMethod(point.getSignature().getName());
//        sysLog.setRequestUri(URLUtil.getPath(request.getRequestURI()));
//        sysLog.setHttpMethod(HttpMethod.get(request.getMethod()));
//        // 参数
//        Object[] args = point.getArgs();
//        sysLog.setParams(getText(JSONObject.toJSONString(args)));
//
//        sysLog.setStartTime(LocalDateTime.now());
//        sysLog.setUa(request.getHeader("user-agent"));
//
//        // 发送异步日志事件
//        Object obj = point.proceed();
//
//        R r = Convert.convert(R.class, obj);
//        if (r.getIsSuccess()) {
//            sysLog.setType(LogType.OPT);
//        } else {
//            sysLog.setType(LogType.EX);
//            sysLog.setExDetail(r.getMsg());
//        }
//        if (r != null) {
//            sysLog.setResult(getText(r.toString()));
//        }
//
//        sysLog.setFinishTime(LocalDateTime.now());
//        long endTime = Instant.now().toEpochMilli();
//        sysLog.setConsumingTime(endTime - startTime);
//
//        applicationContext.publishEvent(new SysLogEvent(sysLog));
//        return obj;
//    }


}
