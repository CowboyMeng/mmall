package com.mmall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @Author: Chou_meng
 * @Date: 2017/9/28
 */
public class BigDecimalTest {

    @Test
    public void test1() {
        System.out.println(0.05 + 0.01);
    }

    @Test
    public void test2() {
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        System.out.println(b1.add(b2));
    }

    @Test
    public void test3() {
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }

}
