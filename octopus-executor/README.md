# Octopus 执行器

通过配置文件执行爬虫

#### 构建

```bash
cd octopus

mvn -X clean package -DskipTests=true

```

#### 使用

1. 解压缩 octopus-executor/target/octopus-executor-release.tar.gz
2. 在**sites**目录下新增爬虫配置文件
3. 执行命令 `./octopus.sh xxx`, `xxx` 代表配置文件名称