# dataj4mysql 简介
dataj4mysql 是一款通过http接口实现任意数据写入msyql工具，支持自动识别JSON节点，自动建立字段，支持增量更新，提供web控制台增加单一索引，重置字段类型，能够很方便的把json数据同步到mysql。dataj4mysql使用io3x独创AIC(All In classes)开发架构模式,极其简单轻量代码实现json到mysql的管理并附带一套基于yaml配置的java接口文档系统,结构如下

```
│  deploy.bat
│  LICENSE
│  pom.xml
│  README.md
│
├─deploy
└─src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      └─github
    │  │          └─io3x
    │  │              │  myApplication.java
    │  │              │
    │  │              ├─api
    │  │              │  │  docController.java
    │  │              │  │
    │  │              │  └─classes
    │  │              │          funcApi.java
    │  │              │
    │  │              ├─app
    │  │              │  │  func.java
    │  │              │  │  WebConfig.java
    │  │              │  │
    │  │              │  ├─libs
    │  │              │  │  ├─classes
    │  │              │  │  │      anyTree.java
    │  │              │  │  │      db.java
    │  │              │  │  │
    │  │              │  │  └─lock
    │  │              │  │          dbTableLock.java
    │  │              │  │          internLock.java
    │  │              │  │
    │  │              │  ├─sboot
    │  │              │  │  │  dbRecordRunner.java
    │  │              │  │  │  R.java
    │  │              │  │  │
    │  │              │  │  ├─Config
    │  │              │  │  │      WebSecurityConfig.java
    │  │              │  │  │
    │  │              │  │  └─Interceptor
    │  │              │  │          SecurityInterceptor.java
    │  │              │  │
    │  │              │  └─Utils
    │  │              │          CollectorUtils.java
    │  │              │          RequestParamsToMap.java
    │  │              │          StrUtils.java
    │  │              │
    │  │              ├─config
    │  │              │      CorsConfig.java
    │  │              │      TaskExcutor.java
    │  │              │
    │  │              ├─demo
    │  │              │      indexController.java
    │  │              │
    │  │              └─syncd
    │  │                  │  adminController.java
    │  │                  │  Cron.java
    │  │                  │  manualController.java
    │  │                  │  toolController.java
    │  │                  │
    │  │                  └─classes
    │  │                          asyncHandleEvent.java
    │  │
    │  └─resources
    │      │  application.yml
    │      │  banner.txt
    │      │  logback-spring.xml
    │      │
    │      ├─static
    │      └─templates
    │          │  doc.yaml
    │          │
    │          ├─api
    │          │  └─doc
    │          │          doc.html
    │          │          header.html
    │          │          index.html
    │          │          widget.html
    │          │
    │          └─syncd
    │              │  R.html
    │              │
    │              └─admin
    │                      footer.html
    │                      header.html
    │                      index.html
    │                      metadata.html
    │
    └─test
        └─java

```

## 封面

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/80DB0E80A6310B5CB1505472E7C070E2.png)

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/6A02CE0663F7625CC41F03EE4E3ABF27.png)

## 基本约定
1. 系统不能识别的字段类型默认使用text,可通过接口界面一键修改 见 http://localhost:13306/syncd/admin/index  
2. JSON结构前后不一致时,默认使用0填充,系统暂时不支持datetime等时间日期类型(只能算text或者varchar),否则变更字段类型不通用,如果json中含有时间日期类型还需要做排序处理,建议调用接口前转化成 20200912 这种数据形式
3. 生产部署请使用内网地址或者加入nginx白名单 工具没有内置安全机制
4. 每个生成的mysql数据库表含有主键字段xxx3id索引,json中需要避免重名
5. 当字段不存在时,首次写入需要判断相对耗时较长,自动建立后就很快

## 部署
可以手动编译源码或者直接下载jar包运行即可

- 手动编译


编译 执行 

> call mvn clean

> call mvn package -Dmaven.test.skip=true

1. 复制target下面的dataj4mysql.jar、templates目录到到发布目录,执行jar -jar dataj4mysql.jar 即可启动(启动前请修改配置)
2. windows 系统直接运行 deploy.bat 即可生成jar包在deploy目录下

- 下载编辑好的jar包直接运行



- 修改 applacation.yml mydb节点下面mysql连接信息,然后打开 http://localhost:13306 即可
- 修改 管理平台密码 默认是 admin13306

## 基本功能
### 直接写入json到mysql数据库表 test （该接口无去重限制）

> 接口地址 /syncd/tool/test 或 /syncd/tool/json2one

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/0358E0591571B1D791D20D59C7197C8C.png)

直接入库后如下图

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/E719DF6E65D3659E0141865ADE305646.png)

### 手动维护数据字段类型
> 操作地址 http://localhost:13306/syncd/admin/index

默认不能识别类型使用text,默认值0
支持一键修改字段类型及单一索引,修改数据表备注
默认myisam储存引擎,如果需要修改引擎,需要修改源码找到创建表sql处即可

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/57D35B3F746A4D67942B074DF24D5EFE.png)

### 执行mysql查询语句,返回JSON

> 文档地址 http://localhost:13306/?op=syncd_tool_query

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/79B76A88028F423DB354A6D15E19AECA.png)

### 修改更新表sql语句
> 文档地址 http://localhost:13306/?op=syncd_tool_ddl

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/AFF4A54D8917E22ABF0AD22B9EF31DA8.png)

### 同步JSON到mysql

- 申明的字段值不能重复,且写入数据必须大于已记录的id,否则不会写入
- 申明的字段最好设置下是数字类型,且有索引id

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/5DA9F87733C79F436018C662717A9C7F.png)

> 接口文档地址 http://localhost:13306/?op=syncd_tool_syncd

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/AE08273610CC5DAD896D2A63D3114A80.png)

如当前样例数据的标记字段是:aewme_live_data_id 那么如果存在aewme_live_data_id 如 325 则数据不会写入,如果不存在,但是一批数据id均小于325 这批数据也不会写入,如果需要补数据,可通过页面修改元数据最大记录id

### 修改同步数据最大索引值
只有 http://localhost:13306/syncd/tool/syncd 接口写入的数据才会存在最大索引id的记录,其它是去重模式判断的

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/EE9D11B7C858651A28D4B8B161CB982C.png)

### 同步JSON到mysql-无数字增量
> 文档地址 http://localhost:13306/?op=syncd_tool_syncd2

相对于上一个接口,该接口只判断是否存在 aewme_live_data_id,并不判断id最大值是否超过索引记录

### 同步JSON到mysql-无数字增量-联合字段索引
> 文档地址 http://localhost:13306/?op=syncd_tool_syncd3

![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/1733540EBFC2C5F87082C26A6BCC813E.png)

- 如样例数据,每天的消耗汇总是一条记录,不能重复写入,有ymd和cost两个字段标记,则限制参数传数组union_key[]得传两个,数据表会自动生成一个pkey记录是否存储过,kpey字段名称任意,由接口传入的new_key名称决定
- 暂时有个bug,就是同一批数据的时候还是会记录两条数据


## 内置无入侵无js依赖io3x-doc 接口文档使用说明
直接使用yaml格式配置文件定义文档,freemarker模型引擎翻译,使用html自带表单提交到iframe内置页面进行调试
### 每个接口定义一个标识
如
```yaml
demotest:
    title: 这是标题
    url: 这是url地址
    more: |
        说明：
        视频普通上传为了2个步骤 1.上传视频获取提取码 2.通过提取码获取转码后的视频和缩略图地址
        步骤1:返回成功标识 识别ret="yes" 记住sn码 通过视频提取接口获取转码后的视频
        {
            "ret": "yes",
            "msg": "ok",
            "ser": "H-P",
            "ticket": "0VE7N13EA5SLXI6N",
            "info": {
                "sn": "KLBW42UOQOVG55YR"
            }
        }
    b_fds: |
        access_token|这里是默认值|授权码
        img||图片上传|file
        more||多行文本|textarea
        tags[]||标签1
        tags[]||标签2
        tags[]||标签3
    e_fds: |
        isure||是否确定(11是,10否)
```
### 格式说明

- demotest 标识一个接口标识
- title 接口标题
- url 调用的地址:可以是内部地址路径如/xx/xx 也可以是其它项目地址 如http://www.io3x.com/api/xx/xx
- more 接口的一些说明,想写什么就写什么,很简单的就可以支持多行
- b_fds 节点下面表示必填字段,使用|字符分额,第一个是字段名称,第二个是默认值,第三个字段说明,第四个是区分是当行(默认不填)多行(textarea)还是文件（file）
- e_fds 同上

### 直接编辑header.html 加入菜单
如 

```html
<a class="nav-menu-item" href="?op=demotest">io3x-doc基本使用样例</a>
```

### 实际展示效果
![](https://diaox.oss-cn-shanghai.aliyuncs.com/uploadfile/md/202009/0912/A5AE5D78AFAC6A4A083A226A162F7495.png)


## 项目依赖支持特别感谢
- springboot及相关套件
- jfinal mysql操作模型 com.liucf.db.record
- Kimochi.css 接口文档UI


## 更新说明

### 1.1.12 通过web管理界面 http://localhost:13306/syncd/admin/index 支持把默认text修改为 datetime类型
- 修改前,如果之前是0填充的数据,整行数据会被删除
- 写入数据必须符合 2020-01-01 00:00:00 形式格式,否则不能录入数据库

### 1.1.23 支持通过管理界面复制数据库结构及内容;支持通过配置文件修改默认数据库类型
- 由于TIDB不支持字段类型的任意修改,该工具暂时不能应用于TIDB数据库