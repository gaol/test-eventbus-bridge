# test-eventbus-bridge
A vertx eventbus bridge that is used for testing

## How to used it


```python
import unittest
from testeventbus import EventBusBridgeStarter
from vertx.eventbus import EventBus


class EventBusClientTests(unittest.TestCase):
    """
    This is the tests against a local test eventbus bridge
    """
    
    def __init__(self, *args, **kwargs):
        super(EventBusClientTests, self).__init__(*args, **kwargs)
    
    @classmethod
    def setUpClass(cls):
        print("Start EventBus Bridge")
        cls.starter = EventBusBridgeStarter(debug=True)
        cls.starter.start()
        cls.starter.wait_started()
        print("yes, Bridge started !!")
    
    @classmethod
    def tearDownClass(cls):
        print("Now stop the bridge")
        cls.starter.stop()
    
    def test_send(self):
        print("Testing send")
        ebus = EventBus({"connect": True})
        ebus.wait()
        ebus.send("echo", body={"hello": "world"})
```
