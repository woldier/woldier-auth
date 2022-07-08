package com.woldier.auth.context;

import com.woldier.auth.utils.NumberHelper;
import com.woldier.auth.utils.StrHelper;

import java.util.HashMap;
import java.util.Map;
/**
 * 相当于线程上下文（一次服务的上下文）
 *
 * 获取当前域中的 用户id appid 用户昵称
 * 注意： appid 通过token解析，  用户id 和 用户昵称必须在前端 通过请求头的方法传入。 否则这里无法获取
 */
public class BaseContextHandler {
    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 为map添加key：value ，无论value为什么值 都会转化为
     * @param key
     * @param value
     */
    /*一系列可重载的set方法*/
    public static void set(String key, Long value) {
        Map<String, String> map = getLocalMap();
        map.put(key, value == null ? "0" : String.valueOf(value));
    }

    public static void set(String key, String value) {
        Map<String, String> map = getLocalMap();
        map.put(key, value == null ? "" : value);
    }

    public static void set(String key, Boolean value) {
        Map<String, String> map = getLocalMap();
        map.put(key, value == null ? "false" : value.toString());
    }
/*-----------------------------------------------------------*/

    /**
     *
     * 获取线程中保存的map
     * @return
     */
    public static Map<String, String> getLocalMap() {
        Map<String, String> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>(10);
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 将内存中的map设置给本线程
     * @param threadLocalMap
     */
    public static void setLocalMap(Map<String, String> threadLocalMap) {
        THREAD_LOCAL.set(threadLocalMap);
    }

    /**
     * 通过key值得到map中存放的数据
     * @param key
     * @return
     */

    public static String get(String key) {
        Map<String, String> map = getLocalMap();
        return map.getOrDefault(key, "");
    }

    /**
     * 账号id（Token）
     *
     * @return
     */
    public static Long getUserId() {
        Object value = get(BaseContextConstants.JWT_KEY_USER_ID);
        return NumberHelper.longValueOf0(value);
    }

    /**
     * 设置账号id（Token）
     *
     * @param userId
     */
    public static void setUserId(Long userId) {
        set(BaseContextConstants.JWT_KEY_USER_ID, userId);
    }

    public static void setUserId(String userId) {
        setUserId(NumberHelper.longValueOf0(userId));
    }

    /**
     * 账号表中的name（Token）
     *
     * @return
     */
    public static String getAccount() {
        Object value = get(BaseContextConstants.JWT_KEY_ACCOUNT);
        return returnObjectValue(value);
    }

    /**
     * 账号表中的name（Token）
     *
     * @param name
     */
    public static void setAccount(String name) {
        set(BaseContextConstants.JWT_KEY_ACCOUNT, name);
    }


    /**
     * 登录的账号（Token）
     *
     * @return
     */
    public static String getName() {
        Object value = get(BaseContextConstants.JWT_KEY_NAME);
        return returnObjectValue(value);
    }

    /**
     * 登录的账号（Token）
     *
     * @param account
     */
    public static void setName(String account) {
        set(BaseContextConstants.JWT_KEY_NAME, account);
    }

    /**
     * 获取用户token（Token）
     *
     * @return
     */
    public static String getToken() {
        Object value = get(BaseContextConstants.TOKEN_NAME);
        return StrHelper.getObjectValue(value);
    }

    /**
     * 设置用户token（Token）
     * @param token
     */

    public static void setToken(String token) {
        set(BaseContextConstants.TOKEN_NAME, token);
    }

    public static Long getOrgId() {
        Object value = get(BaseContextConstants.JWT_KEY_ORG_ID);
        return NumberHelper.longValueOf0(value);
    }

    public static void setOrgId(String val) {
        set(BaseContextConstants.JWT_KEY_ORG_ID, val);
    }


    public static Long getStationId() {
        Object value = get(BaseContextConstants.JWT_KEY_STATION_ID);
        return NumberHelper.longValueOf0(value);
    }

    public static void setStationId(String val) {
        set(BaseContextConstants.JWT_KEY_STATION_ID, val);
    }

    public static String getDatabase() {
        Object value = get(BaseContextConstants.DATABASE_NAME);
        return StrHelper.getObjectValue(value);
    }


    public static void setDatabase(String val) {
        set(BaseContextConstants.DATABASE_NAME, val);
    }


    /**
     * 将map中取出的值转换为string
     * @param value
     * @return
     */

    private static String returnObjectValue(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     *
     *
     * 移除此线程局部变量的当前线程值。
     * 如果这个线程局部变量随后被当前线程读取，它的值将通过调用它的initialValue方法重新初始化，除非它的值在此期间是由当前线程设置的。
     * 这可能导致在当前线程中多次调用initialValue方法。
     *
     * 为了保证不会发生内存泄露 initialValue方法会在interceptor中的preHandel中进行局部变量初始化。
     * 然后本方法一般是再interceptor的complete方法中进行调用
     */
    public static void remove() {
        if (THREAD_LOCAL != null) {
            THREAD_LOCAL.remove();
        }
    }

}
