## JSON-Config 

[![Build Status](https://travis-ci.org/binaryoverload/JSON-Config.svg?branch=master)](https://travis-ci.org/binaryoverload/JSON-Config)  [![JitPack Version](https://jitpack.io/v/BinaryOverload/JSON-Config.svg)](https://jitpack.io/#BinaryOverload/JSON-Config) [![documenation](https://img.shields.io/badge/documentation-available-brightgreen.svg)](https://binaryoverload.github.io/JSON-Config/jsonconfig/)

## Add as a dependency
Maven:
```xml
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
</repository>
...
<dependency>
    <groupId>com.github.binaryoverload</groupId>
    <artifactId>JSON-Config</artifactId>
    <version>@VERSION@</version>
</dependency>
```

Gradle:
```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile group: 'com.github.binaryoverload', name: 'JSON-Config', version: 'VERSION'
}
```

## Example
```java
public static void main(String[] args) {
    JSONConfig config = new JSONConfig("config.json");
    
    if (config.getString("bot.token") != null)
        init(config.getString("bot.token"));
    else {
        throw new IllegalStateException("You need the token in order to start the bot!");
        System.exit(1);
    }
}
```

Example config:
```json
{
  "bot": {
    "token": "Amazing.Bot.Token"
  }
}
```
