package com.woldier.auth.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间工具类
 */
public class TimeUtils {

    /**
     * 获取密码输出错误的等待时间
     * @param time 传入时间
     * @return 转为date
     */
    public static LocalDateTime getPasswordErrorLockTime(String time) {
        if (time == null || "".equals(time)) {
            return LocalDateTime.MAX;
        }
        if ("0".equals(time)) {
            /**返回最大时间 当天不允许在登陆*/
            return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        }
        /*传为char*/
        char unit = Character.toLowerCase(time.charAt(time.length() - 1));

        if (time.length() == 1) {
            unit = 'd';
        }
        Long lastTime = NumberHelper.longValueOf0(time.substring(0, time.length() - 1));

        LocalDateTime passwordErrorLastTime = LocalDateTime.MAX;
        switch (unit) {
            //时
            case 'h':
                passwordErrorLastTime = LocalDateTime.now().plusHours(lastTime);
                break;
            //天
            case 'd':
                passwordErrorLastTime = LocalDateTime.now().plusDays(lastTime);
                break;
            //周
            case 'w':
                passwordErrorLastTime = LocalDateTime.now().plusWeeks(lastTime);
                break;
            //月
            case 'm':
                passwordErrorLastTime = LocalDateTime.now().plusMonths(lastTime);
                break;
            default:
                passwordErrorLastTime = LocalDateTime.now().plusDays(lastTime);
                break;
        }

        return passwordErrorLastTime;
    }

}


