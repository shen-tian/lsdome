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
import time
import datetime
import json
import PyNUT
import csv
import os

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    )

upsData = {}

def getPowerData():
    
    powerData = {
        "ups.status" : upsData['ups.status'], 
        "batt.charge" : upsData['battery.charge'],
        "output.power" : float(upsData['output.current']) * float(upsData['output.voltage'])}
    return json.dumps(powerData)
    #return "meh"
    
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
            
def pollUps():
    nut = PyNUT.PyNUTClient( host="192.168.1.125", login="monuser", password="password")
    ofile = open(os.path.expanduser('~') + '/powerlog.csv', "ab+")
    writer = csv.writer(ofile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    while 1:
        result = nut.GetUPSVars( "openups" )
        global upsData
        paramList = {'ups.status', 'battery.charge', 'battery.current', 'battery.voltage',
            'battery.temperature', 'input.current', 'input.voltage', 'output.current',
            'output.voltage'}
        outputList = list()
        outputList.append(time.strftime("%Y/%m/%d %H:%M:%S", time.localtime(time.time())))
        for param in paramList:
            upsData[param] = result[param]
            outputList.append(result[param])
        writer.writerow(outputList)
        sleep(1.)
    return    


if __name__ == '__main__':
    import socket
    import threading
    
    d = threading.Thread(target=pollUps)
    d.setDaemon(True)
    d.start()
    
    logging.info('Started polling thread')
    
    address = ('localhost', 5204) # let the kernel give us a port
    server = SocketServer.TCPServer(address, EchoRequestHandler)
    #ip, port = server.server_address # find out what port we were given

    d2 = threading.Thread(target=server.serve_forever)
    d2.setDaemon(True)
    d2.start()
    
    logging.info("Both threads started. Press any key to exit")
    
    x = raw_input('Enter stuff to exit')

    #server.serve_forever()