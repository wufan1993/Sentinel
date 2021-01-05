/*规则表，控制台页面操作写入数据*/
DELETE FROM rule_config;

INSERT INTO rule_config (dict_code, dict_ext,dict_value,pin) VALUES
  ('sentinel-dashboard_flowRuleKey', 'test', '','test');

/*用户应用表，通过h2数据库控制台写入数据*/
DELETE FROM user_app;

INSERT INTO user_app (username, app_name) VALUES
  ('sentinel', 'sentinel-dashboard');
INSERT INTO user_app (username, app_name) VALUES
('test', 'sentinel-dashboard');

/*用户信息权限表，通过h2数据库控制台写入数据*/
DELETE FROM user_info;

INSERT INTO user_info (username, password,phone,privilege_type) VALUES
  ('sentinel', 'sentinel', '18401565725','ALL');
INSERT INTO user_info (username, password,phone,privilege_type) VALUES
  ('test', 'test', '18401565725','READ_RULE,WRITE_RULE');