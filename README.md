# Sentinel: 基于release1.8版本 完成了以sentinel-dashboard为核心的规则推送API模式的设计开发

## 技术方案
### 1. 增加技术
- **mybatis-plus**
- **h2数据库持久化方案(可以随便替换其它数据源，只需要改下配置文件jdbc数据源)**
### 2. 核心思想
- **以h2数据库持久化方案做各种规则信息的记录**
- **以API获取规则模式展示控制台具体的规则数据,实时获取服务机器规则**
- **自定义应用服务器的启动状态，来维护服务器sentinel状态，和控制台的数据库做交互同步**
### 3. 资源整合
- **整合了API中HTTP数据交互和集群流控的jar包，并封装在了统一的sentinel-extension-resource模块下**
- **[注意]：我添加了服务器重启的状态标记，所以一定要使用这个模块，才能完成服务器和控制台规则同步**
## 增加功能
### 1. 主要功能
- **流控规则数据持久化**
- **降级规则数据持久化**
- **热点规则数据持久化**
- **系统规则数据持久化**
- **授权规则数据持久化**
### 2. 增强功能
- **账户登陆信息支持**
- **账户应用权限信息支持**
- **账户应用操作读、写、删权限支持**
- **控制台重启获取数据库规则数据并同步到各个应用服务器**
- **服务器重启去请求客户端获取数据库规则并执行服务器规则刷新命令**
- **定时任务去监控服务器资源状态，打印报警日志。使用者可以自己根据规则发送邮件或者短信报警**

## 接入方式
### 1. 构建包和依赖环境
mvn clean -U install -Dmaven.test.skip=true
在各个模块编译路径下分别获取到 
- **1、sentinel-extension-resource-1.8.0.jar**
- **2、sentinel-dashboard.jar**

### 2. 启动sentinel-dashboard控制台
- **java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar**
- **注意，首次启动需要加载创建数据表结构，以后启动就不需要了，h2会把数据库持久化到文件中，这样重启服务就能加载到数据，否则每次启动控制台更新表结构就没有数据了**
- **操作的文件是application-[env].properties**
### 3. 启动服务应用客户端

在服务应用项目中引入pom jar包
```xml
<!-- 在自己的应用环境中引入sentinel-extension-resource-1.8.0.jar包(如果有maven私服可以上传，没有可以采用import方式导入到工程中) -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-extension-resource</artifactId>
    <version>1.8.0</version>
</dependency>
```

xml文件配置方式注入系统参数bean+切面bean(用注解的可以自己去写，只要注入bean就行)
```xml
<!-- 可选注入bean，没有填写就需要在jvm参数上加上自己的机器名称和客户端的服务地址-->
<bean id="sentinelResourceRegister" class="com.alibaba.csp.sentinel.extension.resource.SentinelResourceRegister" >
 <property name="sentinelServer" value="${sentinel.dashboard.server}"></property>
 <property name="projectName" value="${sentinel.project.name}"></property>
</bean>
```
```xml
<!-- 使用注解切面的方式去控制资源进行流控降级操作 -->
<bean class="com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect"></bean>
```

## 使用方式
### 1. 登陆数据库
- **登陆 http://localhost:8080/h2**
- **填写JDBC URL、User Name、Password 具体参数需要自己在配置文件application.properties中定义**

### 1. 配置启动账户和操作权限
- **直接在h2数据库控制到添加启动账户和操作权限（具体怎么添加数据可以自己摸索）**
- **案例:ID	username	password	phone	可以填写权限(ALL,WRITE_RULE,READ_RULE,DELETE_RULE,READ_METRIC,ADD_MACHINE)**
- **权限就是字符串加上逗号分割，如果全都有填写[ALL],如果只有读和删，那么填写[READ_RULE,DELETE_RULE]**
### 2. 配置账户应用权限
- **案例:ID	username	app_name**
- **app_name是服务器在控制台注册的应用名称，配置好映射关系后，这样这个用户就可以访问这些应用了**