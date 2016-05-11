#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Power management capability: uses NUT to monitor the power state of the system, and
# do smart things based off of that (mostly dim some lights).
# Grabs some data fields (battery charge %, UPS status, and current power drain. Serves
# that up via JSON on at TCP socket. Can probably get fancier, but works for now.

import logging
import sys
import threading
import SocketServer
from time import sleep
import datetime
import json
import PyNUT

logging.basicConfig(level=logging.DEBUG,
                    format='%(name)s: %(message)s',
                    )

# Hard coded, of course... 
nut = PyNUT.PyNUTClient( host="192.168.1.116", login="monuser", password="password")
exit = False

def getPowerData():
    global nut
    result = nut.GetUPSVars( "openups" )
    status = result["ups.status"]
    charge = result["battery.charge"]
    current = float(result["output.current"])
    voltage = float(result["output.voltage"])
    
    powerData = {
        "ups.status" : status, 
        "batt.charge" : charge,
        "output.power" : current * voltage}
    return json.dumps(powerData)

class EchoRequestHandler(SocketServer.BaseRequestHandler):

    def handle(self):
        logging.info('New socket connection')
        try:
            while True:
                data = self.request.recv(1024)
                self.request.send(getPowerData())
        finally:
            logging.info('Closing')
            return


if __name__ == '__main__':
    import socket
    import threading
    

    address = ('localhost', 5204) # let the kernel give us a port
    server = SocketServer.TCPServer(address, EchoRequestHandler)
    ip, port = server.server_address # find out what port we were given

    server.serve_forever()