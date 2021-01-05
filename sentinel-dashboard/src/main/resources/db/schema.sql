DROP TABLE IF EXISTS rule_config;
CREATE TABLE rule_config
(
  id int NOT NULL auto_increment COMMENT '主键ID',
  dict_code VARCHAR(100) NULL DEFAULT NULL COMMENT '字典码唯一',
  dict_ext VARCHAR(100) NULL DEFAULT NULL COMMENT '字典描述',
  dict_value MEDIUMTEXT NULL DEFAULT NULL COMMENT '字典内容',
  pin VARCHAR(500) NULL DEFAULT NULL COMMENT '用户',
  ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间戳',
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_app;
CREATE TABLE user_app
(
  id int NOT NULL auto_increment COMMENT '主键ID',
  username VARCHAR(100) NULL DEFAULT NULL COMMENT '用户信息',
  app_name VARCHAR(100) NULL DEFAULT NULL COMMENT '应用信息',
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info
(
  id int NOT NULL auto_increment COMMENT '主键ID',
  username VARCHAR(100) NULL DEFAULT NULL COMMENT '用户名称',
  password VARCHAR(100) NULL DEFAULT NULL COMMENT '用户密码',
  phone VARCHAR(100) NULL DEFAULT NULL COMMENT '联系电话',
  privilege_type VARCHAR(500) NULL DEFAULT NULL COMMENT '权限',
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS metric_detail;
CREATE TABLE `metric_detail` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` DATETIME COMMENT '创建时间',
  `gmt_modified` DATETIME COMMENT '修改时间',
  `app` VARCHAR(100) COMMENT '应用名称',
  `timestamp` DATETIME COMMENT '统计时间',
  `resource` VARCHAR(500) COMMENT '资源名称',
  `pass_qps` INT COMMENT '通过qps',
  `success_qps` INT COMMENT '成功qps',
  `block_qps` INT COMMENT '限流qps',
  `exception_qps` INT COMMENT '发送异常的次数',
  `rt` DOUBLE COMMENT '所有successQps的rt的和',
  `_count` INT COMMENT '本次聚合的总条数',
  `resource_code` INT COMMENT '资源的hashCode',
/*  INDEX app_idx(`app`) USING BTREE,
  INDEX resource_idx(`resource`) USING BTREE,
  INDEX timestamp_idx(`timestamp`) USING BTREE,*/
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;