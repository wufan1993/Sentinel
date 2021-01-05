package com.alibaba.csp.sentinel.dashboard.h2.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * 我本非凡
 * Date:2020-12-23
 * Time:21:12:54
 * Description:RuleConfig.java
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
public class RuleConfig {

    @TableId(value = "id",type = IdType.INPUT)
    private Integer id;
    private String dictCode;
    private String dictExt;
    private String dictValue;
    private String pin;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictExt() {
        return dictExt;
    }

    public void setDictExt(String dictExt) {
        this.dictExt = dictExt;
    }

    public String getDictValue() {
        return dictValue;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
