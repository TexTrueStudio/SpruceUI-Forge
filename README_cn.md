<p align="center">
 <img width="100px" src="src/main/resources/icon.png" align="center" alt="SpruceUI-Forge port(Unofficial) Logo" />
 <h2 align="center">ObsidianUI</h2>
 <p align="center">SpruceUI unofficial forge port. A Minecraft mod API which adds some GUI utilities.</p>


[English](README.md)

## 构建

只需执行 `./gradlew build` 。

## 在MOD内使用

你可以看一下 [SpruceUI-Forge 测试模组（目前不可用）](https://github.com/LambdAurora/SpruceUI/tree/1.16/src/testmod) 的使用实例。

### 在项目中导入

除了基本的 Forge mod `build.gradle` 之外，还可以把它添加到你的 `build.gradle` 中：

#### Architectury Loom：

```groovy
repositories {
    mavenLocal()
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
}
dependencies {
    modImplementation include("maven.modrinth:spruceui-forge:${project.spruceui_version}")
}
```

并将此加入你的 `gradle.properties` ：

```properties
spruceui_version=mc1.19-v0.1.0
```

它将JAR-in-JAR SpruceUI，这样你的mod的用户就不需要单独下载它了。
