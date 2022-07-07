package com.woldier.auth.log.event;

import java.util.function.Consumer;

import com.woldier.auth.context.BaseContextHandler;
import com.woldier.auth.log.entity.OptLogDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
/**
 * 异步监听日志事件
 */

/**
 * 这里的SysLogListener没有注册为组件 因此需要自己集成本组件后 将其注册
 */
@Slf4j
@AllArgsConstructor
public class SysLogListener {
    //private String database;
    /**
     * 函数式接口 ，由第三方应用实现 ，
     */
    private Consumer<OptLogDTO> consumer;

    @Async//异步
    @Order
    @EventListener(SysLogEvent.class)
    public void saveSysLog(SysLogEvent event) {
        /*转OptLogDTO*/
        OptLogDTO optLog = (OptLogDTO) event.getSource();
        //BaseContextHandler.setDatabase(database);

        /**
         * 这里需要集成本组件的系统 自信设置相应的保存逻辑
         * */
        consumer.accept(optLog);
    }
}
