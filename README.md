## JSON-Config [![Build Status](https://travis-ci.org/binaryoverload/JSON-Config.svg?branch=master)](https://travis-ci.org/binaryoverload/JSON-Config) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/099d96844fad4e91895e1436eb16eace)](https://www.codacy.com/app/wegg7250/JSON-Config?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=binaryoverload/JSON-Config&amp;utm_campaign=Badge_Grade)

## Example
```java
public static void main(String[] args) {
    JSONConfig config = new JSONConfig("config.json");
    
    if (config.getString("bot.token").isPresent())
        init(config.getString("bot.token").get();
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
