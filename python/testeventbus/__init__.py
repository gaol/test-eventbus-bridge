#!/usr/bin/python
# Authors:
# 2020: Wolfgang Fahl https://github.com/WolfgangFahl
# 2021: Lin Gao https://github.com/gaol
#
# This test starter borrows lots from https://github.com/rc-dukes/vertx-eventbus-python, thanks to Wolfgang Fahl
#

import os
import time
from subprocess import Popen, PIPE
from threading import Thread

import requests


class EventBusBridgeStarter:
    """
    An Vertx EventBus Bridge Starter, used to start a bridge server for integration testing
    """
    
    def __init__(self, jar_version='1.0.0', port=7000, wait_for='Welcome to use EventBus Starter', debug=False, conf=None):
        """
        construct me

        Args:
           jar_version(str: the bridge jar version to use
           port(int): the port to listen to, defaults to 7000
           wait_for(str): the output string on stderr of the java process to wait for
           debug(bool): True if debugging output should be shown else False - default: False
        """
        self.port = port
        self.wait_for = wait_for
        self.process = None
        self.started = False
        self.debug = debug
        self.conf = conf
        self.jar_file = "%s/test-ebridge-%s-fat.jar" % (os.getcwd(), jar_version)
        self.jar_url = "https://github.com/gaol/test-eventbus-bridge/releases/download/%s/test-ebridge-%s-fat.jar" % (jar_version, jar_version)

    def start(self):
        try:
            if not os.path.exists(self.jar_file):
                if self.debug:
                    print("Downloading bridge jar from %s" % self.jar_url)
                req = requests.get(self.jar_url)
                with open(self.jar_file, 'wb') as f:
                    f.write(req.content)
                    f.close()
            if self.conf is None:
                self.process = Popen(['java', '-jar', self.jar_file], stderr=PIPE)
            elif type(self.conf) is dict or os.path.exists(self.conf):
                self.process = Popen(['java', '-jar', '-conf', self.conf, self.jar_file], stderr=PIPE)
            t = Thread(target=self._handle_output)
            t.daemon = True  # thread dies with the program
            t.start()
            # you need to wait for started with expected output
        except IOError as e:
            print(e)
            raise e
    
    def wait_started(self, time_out=30.0, time_step=0.1):
        """ wait for the java server to be started

        Args:
          time_out(float): the timeout in secs after which the wait fails with an Exception
          time_step(float): the time step in secs in which the state should be regularly checked

        :raise:
           :Exception: wait timed out
        """
        time_left = time_out
        while not self.started and time_left > 0:
            time.sleep(time_step)
            time_left = time_left - time_step
        if time_left <= 0:
            raise Exception("wait for start time_out after %.3f secs" % time_out)
        if self.debug:
            print("wait for start successful after %.3f secs" % (time_out - time_left))
    
    def _handle_output(self):
        """ handle the output of the java program"""
        out = self.process.stderr
        for bline in iter(out.readline, b''):
            line = bline.decode('utf8')
            if self.debug:
                print("java: %s" % line)
            if self.wait_for in line:
                self.started = True
        out.close()
    
    def stop(self):
        if self.started:
            self.process.kill()
            self.started = False
