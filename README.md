# Component Initializer（组件初始化器）
 在Android组件化架构中用于各个组件在Application启动时进行初始化操作的框架
## 组件初始化的各个方案对比
组件初始化的各个方案对比
## 为什么要使用Component Initializer
#### 1. 使用注解来标记Component类

```java
@Component
public class Component implements IComponent {
    @Override
    public void init(Context context) {

    }
}
```

相对于配置文件（xml,或者.MF）,注解方式是最方便的

#### 2. 可以配置组件的初始化依赖
有一种场景是组件A的初始化依赖于组件B先初始化完成，此时我们可以通过@Component注解的dependencies属性来配置这种依赖关系

```java
@Component(
        name = "ComponentA",
        dependencies = {"ComponentB"}
)
public class ComponentA implements IComponent {
    @Override
    public void init(Context context) {
       
    }
}
```

我们的框架会保证ComponentB在ComponentA之前初始化

#### 3. 不使用反射，效率高
我们是通过注入new ComponentA()代码来实现ComponentA类的初始化，不是通过反射，所以效率高

#### 4. 使用简便
只需要一行代码

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ComponentInitializer.initComponents(this);
    }
}
```

## 使用方式
### 1.添加依赖
- #### 添加gradle plugin 
Project级别的build.gradle文件
```gradle
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath "com.jst.componentinitializer:gradle-plugin:1.0.0"
    }
}
```
App module(使用'com.android.application' plugin的module)的build.gradle文件

```gradle
apply plugin: 'com.jst.component.initializer'
```

- #### 添加sdk 和 annotationProcessor

组件module （使用@Component注解的module）

```gradle
dependencies {
    implementation 'com.jst.componentinitializer:componentinitializer:1.0.0'
    annotationProcessor 'com.jst.componentinitializer:compiler:1.0.0'
}
```
Application类所在moudule（一般为App moudule）

```gradle
dependencies {
    implementation 'com.jst.componentinitializer:componentinitializer:1.0.0'
}
```

### 2.创建组件类
在组件module中新增加一个Component类，该类实现IComponent接口，添加@Component注解

```java
@Component
public class Component implements IComponent {
    @Override
    public void init(Context context) {

    }
}
```
大部分的组件不需要添加初始化依赖，所以这样配置就可以了

- #### 添加初始化依赖
如果一个组件的初始化依赖于另一个组件的初始化先完成，则需要添加初始化依赖。

在dependencies中添加被依赖组件的 component name。

依赖可以有多个，dependencies是数组。

```java
@Component(
        dependencies = {"ComponentB","ComponentC"}
)
public class ComponentA implements IComponent {
    @Override
    public void init(Context context) {
       
    }
}
```
对于被依赖的组件，则需要声明一个component name 

```java
@Component(
        name = "ComponentB"
)
public class ComponentB implements IComponent {
    @Override
    public void init(Context context) {
       
    }
}
```
### 3.在Application类中执行初始化

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ComponentInitializer.initComponents(this);
    }
}
```

## @Component注解dependencies参数配置异常检测
对于配置了dependencies参数的场景会存在一些异常情况。对于所有的异常情况，该框架在执行ComponentInitializer.initComponents()方法时都会进行检测，
并且以IllegalArgumentException异常的形式进行抛出，并且会提示给用户异常原因所在的具体的类，方便用户定位问题。

- #### 异常情况1：dependencies存在循环依赖
例如：ComponentA 依赖 ComponentB，ComponentB 依赖 ComponentC ,ComponentC 依赖 ComponentA，则存在循环依赖

- #### 异常情况2：dependencies配置的component name 不存在
比如，被依赖的组件没有配置component name，或者配置的component name与你声明的不匹配

- #### 异常情况3：不同的@Component 配置了相同的 component name 

- #### 异常情况4：dependencies配置的component name 是它自己

 
## 该框架所用到的关键技术
- #### 自定义annotationProcessor
在编译期，使用annotationProcessor来处理@Component注解，自动生成.java类

- #### 使用Transform API
使用Transform API 自动添加注册代码到ComponentInitializer类中，这样就可以省去用户手动注册Component的麻烦。其中用到了AutoRegister的能力。
（ARouter也用了AutoRegister）

- #### 使用有向无环图的拓扑排序算法来生成具有依赖关系的component的初始化顺序

## 原理解析
详细的原理解析 请参考 Component Initializer原理解析


## 组件初始化的各个方案对比
如果组件有一些功能需要在Application启动时进行初始化，我们可以如何来实现呢？

- #### 方案1：在app module中去做各组件的初始化
我们知道app module是会依赖于所有的组件module,所以可以在app module中的Application类中去做组件module的初始化。

缺点：
1. 在组件化架构中，一般app module起到的作用是一个app壳，里面应该避免包含具体业务逻辑
2. 各个组件module的初始化代码都放在一个Application类中去做的话，时间一长，Application类就会存在中心化的问题，该类的代码就会变的比较多，逻辑会越来越复杂，该类就很难维护，每个人都不敢乱改其中的代码

- #### 方案2：在各个组件中都增加一个类，例如Component类,该类有一个init方法负责该组件的初始化
同样是在app module中的Application类中调用各个组件的Component类的init方法


优点：  
初始化逻辑放在了各个组件里来维护，解决了Application类的中心化问题

缺点：  
在Application类中要存在这样的一个方法

```java
void initComponents(){
new Component1().init();
new Component2().init();
new Component3().init();
...
}
```
如果有新增的组件，就需要在该方法中新增一行调用代码，手动的维护这样的一个列表非常的不方便

- #### 方案3：在方案2的基础上，在每个组件module中增加一个component.xml的配置文件，在该配置文件中来配置该组件module的Component类的完整类名
实现一个ComponentManager管理类，Application启动时调用该管理类的init方法，在该方法中去扫描所有的component.xml的配置文件（component.xml配置文件会被打包到apk中），解析出其中所有的Component类的完整类名，通过反射初始化该类，然后调用该类的init方法。


优点：  
不需要在Application类中维护一个Component列表

缺点：
1. 需要在组件module中增加一个配置文件来配置Component类的完整类名，这种配置方式并不方便
2. 通过反射来初始化Component类，性能较低

