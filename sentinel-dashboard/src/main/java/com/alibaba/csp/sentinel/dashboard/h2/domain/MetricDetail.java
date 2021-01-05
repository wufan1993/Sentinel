package com.alibaba.csp.sentinel.dashboard.h2.domain;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.Date;

/**
 * 我本非凡
 * Date:2020-12-25
 * Time:15:12:58
 * Description:MetricDetail.java
 *
 * @author wufan02
 * @since JDK 1.8
 * Enjoy a grander sight By climbing to a greater height
 */
public class MetricDetail implements Serializable {

    private static final long serialVersionUID = 7200023615444172715L;

    /**
     * id，主键
     */
    @TableId(value = "id",type = IdType.INPUT)
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 应用名称
     */
    private String app;

    /**
     * 统计时间
     */
    private Date timestamp;

    /**
     * 资源名称
     */
    private String resource;

    /**
     * 通过qps
     */
    private Long passQps;

    /**
     * 成功qps
     */
    private Long successQps;

    /**
     * 限流qps
     */
    private Long blockQps;

    /**
     * 发送异常的次数
     */
    private Long exceptionQps;

    /**
     * 所有successQps的rt的和
     */
    private Double rt;

    /**
     * 本次聚合的总条数
     */
    private Integer _count;

    /**
     * 资源的hashCode
     */
    private Integer resourceCode;

    public static MetricDetail transferMetricEntity(MetricEntity entity) {
        MetricDetail detail=new MetricDetail();
        detail.setId(entity.getId());
        detail.setGmtCreate(entity.getGmtCreate());
        detail.setGmtModified(entity.getGmtModified());
        detail.setApp(entity.getApp());
        detail.setTimestamp(entity.getTimestamp());
        detail.setResource(entity.getResource());
        detail.setPassQps(entity.getPassQps());
        detail.setSuccessQps(entity.getSuccessQps());
        detail.setBlockQps(entity.getBlockQps());
        detail.setExceptionQps(entity.getExceptionQps());
        detail.setRt(entity.getRt());
        detail.set_count(entity.getCount());
        detail.setResourceCode(entity.getResourceCode());
        return detail;
    }

    public static MetricEntity transferMetricDetail(MetricDetail metricDetail) {
        MetricEntity entity=new MetricEntity();
        entity.setId(metricDetail.getId());
        entity.setGmtCreate(metricDetail.getGmtCreate());
        entity.setGmtModified(metricDetail.getGmtModified());
        entity.setApp(metricDetail.getApp());
        entity.setTimestamp(metricDetail.getTimestamp());
        entity.setResource(metricDetail.getResource());
        entity.setPassQps(metricDetail.getPassQps());
        entity.setSuccessQps(metricDetail.getSuccessQps());
        entity.setBlockQps(metricDetail.getBlockQps());
        entity.setExceptionQps(metricDetail.getExceptionQps());
        entity.setRt(metricDetail.getRt());
        if(metricDetail.get_count()!=null){
            entity.setCount(metricDetail.get_count());
        }
        entity.setResource(metricDetail.getResource());
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public Double getRt() {
        return rt;
    }

    public void setRt(Double rt) {
        this.rt = rt;
    }

    public Integer get_count() {
        return _count;
    }

    public void set_count(Integer _count) {
        this._count = _count;
    }

    public Integer getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(Integer resourceCode) {
        this.resourceCode = resourceCode;
    }
}
