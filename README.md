## 关于本地启动
java版本：java17

本地启动时环境变量：PROJECT_PATH=本机根路径 eg:D:\Projects\code\java\lilypadoc

本地启动时使用配置环境：dev

本地启动时将md文件放在{根目录}/.docs文件夹下

核心配置位于lilypadoc-application目录下：

resources
└── customConfig
    ├── lilypadoc.properties
    └── template.config

如果需要使用git同步文件

你需要更改lilypadoc.properties中的git.remote属性，将其改成git仓库的ssh路径

然后将您的私钥（默认查找id_rsa，可以通过lilypadoc.properties中的git.pri.key.name来修改）置于

lilypadoc-application
  └── resources
      └── customConfig

目录下

本地启动后初始化md 请调用接口
```shell
POST http://localhost:4399/operation/parseAll
```
初始化后生成的html文件默认在

lilypadoc-application
  └── resources
      └── html
          └── docs
目录下

访问`http://127.0.0.1:4399/docs/{文件相对路径}`来进行查阅

`注意`：关于目录的层级后续补充解释

## 关于源码
程序入口类：SpringLilypadoc

启动时会加载extension文件夹下的插件jar包

插件源码位于https://github.com/diodeme/lilypadoc-extension

## 关于打包

目录结构如下

build
├── bin
├── conf
├── content
├── extension
├── lib
├── logs
├── web
└── readme.md

其中bin目录结构如下

bin
├── application.pid  #代表程序是否执行中
├── install.bat #待补充
├── install.sh #待补充
├── nohup.out #启动日志
├── start.bat #win环境启动脚本
├── start.sh #待补充
└── stop.bat #win环境关闭脚本




