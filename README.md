# test-eventbus-bridge
A vertx eventbus bridge that is used for testing

This project is created acting as testing eventbus bridge for
vertx eventbus clients.

One of the goal is to make it is easy to start, like using:

> java -jar target/test-ebridge-1.0.0-SNAPSHOT-fat.jar run -conf config.json

Each implementation of vertx eventbus clients can call the command above to start
the server for testing. Like in Python:

```python
from subprocess import *
process = Popen(['java', '-jar' , "target/test-ebridge-1.0.0-SNAPSHOT-fat.jar", "--conf", "config.json", stderr=PIPE)
```

* It contains some builtin handlers with predefined handlers:

| Address | Handler |
| `echo` | Handler echos back any message sent to bridge. Body is `JsonObject` |
| `time` | Handler sends back current time in the server. Body is `JsonObject`, like:  `{"time": "xxx"}` |
| `list` | Handler sends back information about list of registered handlers. Body is `JsonArray`. |
