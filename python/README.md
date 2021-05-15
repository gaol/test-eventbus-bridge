# test-eventbus-bridge
A vertx eventbus bridge that is used for testing

## How to used it

Install using pip:

> pip install vertx-eventbus-bridge-starter

```python
import unittest
from testeventbus import EventBusBridgeStarter, CountDownLatch
from vertx import EventBus


class EventBusClientTests(unittest.TestCase):
    """
    This is the tests against a local test eventbus bridge
    """

    starter = None

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
        latch = CountDownLatch()
        ebus = EventBus()
        ebus.connect()
    
        def handler(message):
            self.assertEqual(message['body']['hello'], 'world')
            ebus.close()
            latch.count_down()
        ebus.register_handler('echo-back', handler)
        ebus.send("echo", reply_address="echo-back", body={"hello": "world"})
        latch.awaits(5)

```

## TODO

* 
