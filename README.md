# docker-command-build-plugin

## Introduction

一个借助Docker Engine API在Jenkins构建步骤使用多个Docker Command的Jenkins插件

支持参数化Docker URL构建，每个构建项目可以使用不同的Docker Engine

## Getting started

### 打包

执行下面命令打包成hpi格式
> mvn hpi:hpi

### 安装

进到Jenkins后台插件管理上传安装插件
![install-plugin](images/install-plugin.png "install-plugin")

### 配置Docker URL

![docker-url-parameter](images/docker-url-parameter.png "docker-url-parameter")

### Docker命令

![current-command-list](images/current-command-list.png "current-command-list")

- Create container
![create-container](images/create-container.png "create-container")
- Remove container
![remove-container](images/remove-container.png "remove-container")
- Start container
![start-container](images/start-container.png "start-container")
- Stop container
![stop-container](images/stop-container.png "stop-container")


## TODO

Others Command

## Issues

## Contributing

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

