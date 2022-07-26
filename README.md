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

### 开源协议
Apache Licence 2.0 （[英文原文](http://www.apache.org/licenses/LICENSE-2.0.html)）
Apache Licence是著名的非盈利开源组织Apache采用的协议。该协议和BSD类似，同样鼓励代码共享和尊重原作者的著作权，同样允许代码修改，再发布（作为开源或商业软件）。
需要满足的条件如下：
* 需要给代码的用户一份Apache Licence
* 如果你修改了代码，需要在被修改的文件中说明。
* 在延伸的代码中（修改和有源代码衍生的代码中）需要带有原来代码中的协议，商标，专利声明和其他原来作者规定需要包含的说明。
* 如果再发布的产品中包含一个Notice文件，则在Notice文件中需要带有Apache Licence。你可以在Notice中增加自己的许可，但不可以表现为对Apache Licence构成更改。
Apache Licence也是对商业应用友好的许可。使用者也可以在需要的时候修改代码来满足需要并作为开源或商业产品发布/销售。

### 用户权益
* 允许免费用于学习、毕设、公司项目、私活等。
* 对未经过授权和不遵循 Apache 2.0 协议二次开源或者商业化作者将追究到底。
* 若禁止条款被发现有权追讨 **5000** 的授权费。

### Netty Reactor 主从多线程网络模型
![Netty Reactor主从多线程网络模型](https://user-images.githubusercontent.com/68835311/173493168-c87515f9-9f05-43c9-95b4-a0f1d1b6c2de.png)

### 代码介绍
* 消息编解码器 [源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-server/src/main/java/com/yijianguanzhu/transfer/server/codec/MessageToMessageCodec.java)
  * 服务收发消息采用二进制帧，一段完整的二进制数据包由【头部+载体】两部分组成：
    * 头部：4 字节小端整数，表示整条消息（包括自身）长度（字节数）。
    * 载体：被序列化后的负载数据。

* 消息读写半包处理 [源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-server/src/main/java/com/yijianguanzhu/transfer/server/codec/ByteToMessageDecoder.java)
    * 总体思路：
        * 根据消息编解码机制提取头部数据，由头部数据长度判断该条消息是否粘包拆包。
        * 再由头部数据长度，提取有效负载数据。
        * 反序列化负载数据，报错直接关闭连接。

* 心跳检测 [源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-server/src/main/java/com/yijianguanzhu/transfer/server/connector/listener/OnlineListener.java)
    * 总体思路：
        * 由单独线程管理所有 session 心跳
        * 在允许客户端离线的最大间隔内，提醒客户端回复心跳
        * 超过最大离线间隔的客户端将踢下线

* 心跳回复 [源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-server/src/main/java/com/yijianguanzhu/transfer/server/handler/PingPongHandler.java)
    * 服务端收到客户端的心跳响应后
        * 服务端返回心跳响应
        * 记录客户端的最新一次心跳响应时间

* 账号登录处理 [源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-auth/src/main/java/com/yijianguanzhu/transfer/auth/handler/AuthHandler.java)
    * 账号登录成功后总体思路：
        * 首先检索当前节点是否已登录，有则踢下线。
        * 其次通过 `ClusterProxy` 推送到其他节点，如果账号已经登录其他节点，由其他节点通知账号下线。
        * 最后提醒本次登录成功。

* 集群支持 [生产者源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-cluster/src/main/java/com/yijianguanzhu/transfer/cluster/rabbit/RabbitProducer.java) [消费者源码](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-cluster/src/main/java/com/yijianguanzhu/transfer/cluster/rabbit/RabbitConsumer.java)


* 业务Handler支持横向扩展(SPI机制) [源码1](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-common/src/main/java/com/yijianguanzhu/transfer/common/provider/HandlerProvider.java) [源码2](https://github.com/yijianguanzhu/icloud-smart-core-transfer-service/blob/master/icloud-smart-core-transfer-server/src/main/java/com/yijianguanzhu/transfer/server/config/ServerChannelInitializer.java)
    * 总体思路（可参考 `AuthHandler` 类 ）：
        * 业务 handler 实现 `HandlerProvider` 接口
        * 根据排序需要，修改 `HandlerProvider.getOrder()` 返回值
        * 支持 `@Sharable` 注解，注入单例 handler
        

### 配置文件
> application-dev.yml

    transfer-service:
      # 绑定端口号
      port: 8888
      session:
        # 单位/秒，客户端超过这个时间没回复心跳，就关闭连接
        keepalive-interval: 45
      time-wheel:
        # 单位/秒，多久检测一次客户端心跳
        keepalive-interval: 45
        # 单位/秒，客户端超过keepalive-interval这个时间没回复心跳，且没超过这个值，通知客户端发送心跳
        max-keepalive-interval: 120
      cluster:
        rabbit:
          enable: true
    # 使用原生RabbitMQ的配置
    spring:
      rabbitmq:
        host: 127.0.0.1
        port: 5672
        username: admin
        password: admin
        template:
          routing-key: websocket-transfer-routing-key
          exchange: websocket-transfer-exchange
          default-receive-queue: websocket-transfer-queue
    
> bootstrap.yml
    
    #nacos配置
    spring:
      cloud:
        nacos:
          # 注册中心地址
          discovery:
            server-addr: ${NACOS_ADDR:http://127.0.0.1:8848}
            username: ${NACOS_USERNAME:nacos}
            password: ${NACOS_PASSWORD:nacos}
          # 配置中心地址
          config:
            server-addr: ${NACOS_ADDR:http://127.0.0.1:8848}
            prefix: ${NACOS_CONFIG_PREFIX:icloud}
            file-extension: ${NACOS_CONFIG_FORMAT:yaml}
            username: ${NACOS_USERNAME:nacos}
            password: ${NACOS_PASSWORD:nacos}

### 程序运行示例
* 服务端启动
![服务端启动](https://user-images.githubusercontent.com/68835311/173513156-09774442-7ec4-4ac5-88c3-e1ba9a362686.png)
![服务端启动](https://user-images.githubusercontent.com/68835311/173513968-cb7ba883-5d93-4043-86c8-195cf1603dbf.png)

* 客户端启动
    * `token` 参考 [icloud-simple-service](https://github.com/yijianguanzhu/icloud-simple-service) 获取
    ![客户端启动](https://user-images.githubusercontent.com/68835311/173517355-cf78c05d-c99d-477a-a0bb-8717d6e02100.png)
