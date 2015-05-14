# secure-index
这是用于密文检索的辅助工具，其可运行程序已包含在[secure-search](https://github.com/7hat/secure-search)项目中。
#### 基本设计思路
- 使用 AES 加密原文档集合，检索过程不设计原文件。
- 构造安全索引：
 - 关键字入口：使用 HMAC 构建
 - 陷门：使用 HMAC 加密临时生成的 AES 项密钥
 - 文档标号集合密文：使用 AES 项密钥加密 gamma 编码压缩后的文档标号集合
 - 相关度集合密文：使用 AES 项密钥加密 LEB128 编码压缩后的 Paillier 相关度集合密文

#### 功能
- 生成用户密钥
- 加密用户文件和生成安全索引
- 解密用户文件

#### 生成用户密钥
- 用户公钥：N<sup>2</sup>
- 用户私钥：K<sub>1</sub>, K<sub>2</sub>, K<sub>3</sub>, N, g, lambda, miu

其中 K<sub>1</sub> 为 AES 密钥，用于加密解密用户文件；K<sub>2</sub> 和 K<sub>3</sub> 为 HMAC 密钥，用于构造安全索引的入口和陷门；N, g, lambda, miu 和 N<sup>2</sup> 是 Paillier 加密体系的参数，用于计算相关度密文。

#### 加密用户文件和生成安全索引
- 先用 GZIP 压缩，再用 AES 加密用户文件
- 词化、生成明文索引和生成安全索引

#### 解密用户文件
- 先用 AES 解密，再用 GZIP 解压用户文件

#### 使用方法（假定包名为helper.jar）
- 帮助：java -jar helper.jar
- 生成用户密钥：java -jar helper.jar -genkey outputPath
- 加密用户文件和生成安全索引：java -jar helper.jar -secureindex inputPath outputPath keyFile (startId)
- 解密用户文件：java -jar helper.jar -decrypt inputPath outputPath keyFile
