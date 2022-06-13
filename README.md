### icloud smart core transfer service项目介绍
* 没有前端。
* 后端采用Netty开发websocket，支持集群，支持将服务注册到nacos注册中心。
* 整合了springboot，使用它的自动装配等功能，不包含它的webmvc模块，没有servlet，只有纯纯ws协议，不支持restful 此类http协议。
* 后端采用feign与nacos中其他服务通信，支持熔断降级，支持负载均衡。
* 其他服务可到 [icloud-simple-service](https://github.com/yijianguanzhu/icloud-simple-service) 查看，包含网关、认证中心、用户中心服务，为本项目而生，当然也可以用作其他用途。
* 项目使用模块分包，推荐使用[IntelliJ IDEA](https://www.jetbrains.com/idea)打开。
* 服务启动完成后，进入 `icloud-smart-core-transfer-server`模块下的test包，找到 `NettyWebSocketClient` 类对websocket server进行测试。

### 工程结构
``` 
icloud-smart-core-transfer-service
├── icloud-smart-core-auth -- 用户登录模块
├── icloud-smart-core-boot -- 启动类模块
├── icloud-smart-core-client -- feign client模块
├── icloud-smart-core-cluster -- 集群模块
├── icloud-smart-core-common -- 基础工具类模块
├── icloud-smart-core-server -- websocket server模块
├── icloud-smart-core-service-api -- 业务模块api
├    ├── icloud-smart-core-cluster-api -- 集群模块api
└──  └── icloud-smart-core-servler-api -- websocket server模块api 
```
