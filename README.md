### 使用场景和限制
网页上在线查看tomcat的log日志。类似linux的tail -f catalina.out查看日志。

### 打包和使用
可直接导入Eclipse或idea工具。打包为war。放入tomcat下的webapps即可。


### 初衷
想找一个简单的日志文件集中管理查看工具。先后找到了：

1. elk(Elasticsearch , Logstash, Kibana ).发现人家是做搜索引擎的。在使用logstash做日志收集的时候。由于默认多核心并行工作。日志是乱序的。当然可以更改为单核有序。一想到单核瓶颈也就在那里了。
2. psi-probe。代替tomat自己的项目管理。里面有log日志在线查看。对我而言：功能太多，我只想要log在线查看部分。尝试剥离代码。奈何底子差，代码太多。核心是使用jdk自带的RandomAccessFile从末尾读取文本。前端使用ajax记录状态并循环检测文件是否有变化。有变化就读取。这个就windows和linux兼容。后面主要是懒，想直接用调用linux tail 命令的。所以就只搬运了下载代码和table样式。
3. logio。网址：http://logio.org/       github:https://github.com/NarrativeScience/log.io   这个没有试用，安装麻烦就没试。
4. 执行linux的tail -f命令。github:https://github.com/wucao/websocket-tail-demo   。缺点：只是简单的demo。所以直接搬运了这个代码做了改造。

日志集中管理实践：试用linux自带的rsyslog集中管理日志。设置每秒扫描（想要实时）。使用tag标签做标记区分和存储。远程使用%logtag获取标签，分类保存文件到指定目录文件下。缺点：规则难学，版本不通规则不同。幸好新版兼容旧版规则。这个已实践，满足需求：集中日志到一台服务器上。然后用这个web在线查看日志即可。最后还是觉得实时就需要消耗性能为代价（每秒扫描文件）。不理想。放弃。此代码仅仅纪念曾经的探索。

### 注意事项

1. websocket api:好像要tomcat8以上，有些低版本的不支持。具体可以到tomcat下的lib查看有没有websocket的jar包。

2. 项目不需要导入其他jar包。因为tomcat和jdk基本都带有。如：tomcat7没有websocket。就需要导了。
3. pom.xml可删除。不一定要是maven项目。pom.xml里的依赖只是用来写代码测试不报错。没有pom.xml当然也可以直接导入tomcat自带的lib。项目就不报错了。
4. 关于权限：可以自己配置tomcat自带的权限security-constraint。即可实现简单的权限拦截。
5. 只能看linux系统。因为使用了tail命令
6. 要带项目名，不能放在ROOT目录下。（可改，index.html,js代码处加了拼接项目名的处理。若用根目录要修改）


###  目录结构和说明

│  pom.xml（maven依赖，可以删除不要。作为普通的web项目即可）
│  README.md（说明文件）                      
└─src
   └─main
       ├─java
       │  └─com
       │     └─xxg
       │         └─websocket
       │             │  Const.java（常数类）
       │             │  DownloadLogController.java（下载）
       │             │  GetHttpSessionConfigurator.java（在websocket连接时注入http的session）
       │             │  LogFilesController.java（获取日志文件列表类）
       │             │  LogWebSocketHandle.java（使用tail -f类）
       │             │  RequestListener.java（拦截session用于websocket连接时获取session，在web.xml处配置监听）
       │             │  TailLogThread.java（tail的线程）
       │             │  
       │             └─util
       │                     CheckSystemOS.java（检测系统工具类）
       │                     SizeExpression.java（计算文件大小类）
       │                     Utils.java（下载文件类）
       │          
       └─webapp
           │  index.html（文件列表）
           │  tailf.html（tailf显示单个文件）
           │  
           ├─css
           │      tables.css
           │      
           ├─images
           │      loading.gif
           │      page_white_compressed.png
           │      page_white_put.png
           │      
           ├─js
           │      jquery.js
           │      
           └─WEB-INF
                   custom.properties（配置文件。）
                   web.xml