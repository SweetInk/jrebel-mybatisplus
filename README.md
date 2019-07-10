
# jrebel-mybatisplus

Jrebel mybatisplus热加载插件，支持重新加载修改后的SQL映射

(中文|[English](README-en.md))

# 前置条件

 **你的IDE安装了[JRebel插件](https://jrebel.com/software/jrebel/download/prev-releases/)**
 
# 如何使用

已开发IDEA的插件 [jrebel-mybatisplus-idea-plugin](https://github.com/SweetInk/jrebel-mybatisplus-idea-plugin). 安装插件后即可使用，不需要再配置了。

## 构建插件

 ``` shell
git clone git@github.com:SweetInk/jrebel-mybatisplus.git
cd jrebel-mybatisplus
mvn -f jr-mybatisplus/pom.xml clean package
```

将构建好的插件`jrebel-mybatisplus\target\jr-mybatisplus.jar`拷贝至任意目录, 比如: `d:\jrebel\plugin\jr-mybatisplus.jar`

## 使用

打开你的IDE(Intellij IDEA or Eclipse),修改运行配置，增加VM参数:`-Drebel.plugins=d:\jrebel\plugin\jr-mybatisplus.jar`，然后以JRebel方式启动

检查插件是否生效:

修改你项目中的mapper xml 文件后，重新编译，如果重新请求接口，你应该会看到控制台输出 “Reloading SQL maps”



# 参考

[Custom JRebel plugins](http://manuals.zeroturnaround.com/jrebel/advanced/custom.html#jrebelcustom)

[Getting Started with Javassist](http://www.javassist.org/tutorial/tutorial.html)
