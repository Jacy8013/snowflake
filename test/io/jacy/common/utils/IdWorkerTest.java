package io.jacy.common.utils;

/**
 * 测试类
 * Created by Jacy on 2016/11/18.
 */
public class IdWorkerTest {
    public static void main(String[] args) {
        Long id = IdWorker.instance.getId();
        System.out.println(id);
    }
}