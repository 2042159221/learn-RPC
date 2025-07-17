# Ming RPC Framework 文档中心

欢迎来到 Ming RPC Framework 的文档中心！这里提供了完整的项目文档，帮助你快速了解和使用这个企业级分布式RPC框架。

## 📚 文档导航

### 🚀 快速开始
- **[项目概述](README.md)** - 了解项目背景、特性和快速开始指南
- **[开发指南](开发指南.md)** - 详细的开发流程、配置说明和最佳实践

### 🏗️ 深入了解
- **[技术架构详解](技术架构详解.md)** - 深入了解框架的设计原理和核心实现
- **[测试报告](测试报告.md)** - 完整的测试结果、性能指标和质量保证

### 📊 项目总结
- **[项目总结](项目总结.md)** - 项目成果、技术收获和未来发展方向

## 🎯 文档使用指南

### 👨‍💻 开发者路径
如果你是开发者，建议按以下顺序阅读：

1. **[项目概述](README.md)** - 了解项目基本信息
2. **[开发指南](开发指南.md)** - 学习如何使用框架
3. **[技术架构详解](技术架构详解.md)** - 深入理解实现原理
4. **[测试报告](测试报告.md)** - 了解质量保证情况

### 🏢 架构师路径
如果你是架构师或技术负责人，建议重点关注：

1. **[技术架构详解](技术架构详解.md)** - 了解架构设计和技术选型
2. **[测试报告](测试报告.md)** - 评估框架的质量和性能
3. **[项目总结](项目总结.md)** - 了解项目价值和发展方向
4. **[项目概述](README.md)** - 快速了解核心特性

### 🎓 学习者路径
如果你是学习者，想了解RPC框架的实现：

1. **[项目概述](README.md)** - 了解RPC框架的基本概念
2. **[技术架构详解](技术架构详解.md)** - 学习架构设计和实现原理
3. **[开发指南](开发指南.md)** - 通过实践加深理解
4. **[项目总结](项目总结.md)** - 了解开发经验和技术收获

## 📖 文档特色

### 🔍 详细全面
- **完整覆盖**: 从概述到实现，从使用到测试，全方位覆盖
- **深入浅出**: 既有高层架构介绍，也有具体代码实现
- **实用性强**: 提供大量代码示例和配置说明

### 🎨 结构清晰
- **分层组织**: 按照不同角色和需求组织文档结构
- **导航便利**: 提供清晰的文档导航和交叉引用
- **格式统一**: 使用统一的Markdown格式和样式

### 📊 数据丰富
- **性能数据**: 详细的性能测试结果和指标分析
- **测试数据**: 完整的测试覆盖率和质量报告
- **架构图表**: 丰富的架构图和流程图

## 🔧 技术亮点预览

### 核心特性
- ✅ **多种序列化支持**: JDK、JSON、Hessian
- ✅ **负载均衡策略**: 轮询、随机、一致性哈希
- ✅ **容错机制**: 快速失败、故障转移、静默处理
- ✅ **服务注册发现**: ETCD、ZooKeeper、Mock
- ✅ **Spring Boot集成**: 注解驱动、自动配置

### 性能指标
- 🚀 **响应时间**: 0.235ms
- 🚀 **吞吐量**: 4,255 QPS
- 🚀 **内存使用**: 12MB
- 🚀 **启动时间**: 4.2秒
- 🚀 **测试覆盖率**: 85%

### 质量保证
- 🧪 **71个测试用例**: 100%通过率
- 🧪 **多层测试**: 单元测试、集成测试、端到端测试
- 🧪 **性能测试**: 并发测试、压力测试、稳定性测试
- 🧪 **代码质量**: 静态分析、代码审查

## 🚀 快速体验

### 5分钟快速开始

1. **添加依赖**
```xml
<dependency>
    <groupId>com.ming</groupId>
    <artifactId>ming-rpc-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2. **服务提供者**
```java
@SpringBootApplication
@EnableRpc(needServer = true)
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}

@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        return new User("Hello " + user.getName());
    }
}
```

3. **服务消费者**
```java
@SpringBootApplication
@EnableRpc(needServer = false)
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}

@RestController
public class UserController {
    @RpcReference
    private UserService userService;
    
    @GetMapping("/user/{name}")
    public User getUser(@PathVariable("name") String name) {
        User user = new User();
        user.setName(name);
        return userService.getUser(user);
    }
}
```

4. **配置文件**
```yaml
rpc:
  name: ming-rpc
  version: 1.0
  serverHost: localhost
  serverPort: 8080
  registryConfig:
    registry: MOCK
```

5. **测试运行**
```bash
# 启动Provider
mvn spring-boot:run -pl example-springboot-provider

# 启动Consumer
mvn spring-boot:run -pl example-springboot-consumer

# 测试接口
curl http://localhost:8082/user/张三
```

## 🤝 参与贡献

### 贡献方式
- 🐛 **报告Bug**: 在GitHub Issues中报告问题
- 💡 **功能建议**: 提出新功能的想法和建议
- 📝 **文档改进**: 帮助改进和完善文档
- 🔧 **代码贡献**: 提交Pull Request贡献代码

### 开发环境
- **JDK**: 21+
- **Maven**: 3.6+
- **IDE**: IntelliJ IDEA 或 VS Code

### 联系方式
- **GitHub**: [https://github.com/ming/learn-RPC](https://github.com/ming/learn-RPC)


## 📄 许可证

本项目采用 MIT 许可证，详情请查看 [LICENSE](../LICENSE) 文件。

## 🔄 文档更新

### 版本历史
- **v1.0.0** (2025-07-17): 初始版本发布
  - 完整的RPC框架实现
  - Spring Boot集成
  - 完善的文档体系

### 更新计划
- **v1.1.0**: 添加更多序列化协议支持
- **v1.2.0**: 实现服务熔断机制
- **v2.0.0**: 支持多语言客户端

---

**感谢你对 Ming RPC Framework 的关注！** 

如果这个项目对你有帮助，请给我们一个 ⭐️ Star，这是对我们最大的鼓励！

开始你的 RPC 框架学习之旅吧！ 🚀
