#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Power management capability: uses NUT to monitor the power state of the system, and
# do smart things based off of that (mostly dim some lights).
# Grabs some data fields (battery charge %, UPS status, and current power drain. Serves
# that up via JSON on at TCP socket. Can probably get fancier, but works for now.

import logging
import SocketServer
import threading
import time
import json
import PyNUT
import csv
import sys

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    )

upsData = {}


def get_power_data():
    power_data = {
        "ups.status": upsData['ups.status'],
        "batt.charge": upsData['battery.charge'],
        "output.power": float(upsData['output.current']) * float(upsData['output.voltage'])}
    return json.dumps(power_data)


class EchoRequestHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        logging.info('New socket connection')
        try:
            while True:
                self.request.recv(1024)
                self.request.send(get_power_data())
        finally:
            logging.info('Closing')
            return


def pollUps(logfile='/var/log/powerlog.csv'):
    # Hardcoded for now.
    nut = PyNUT.PyNUTClient(host="127.0.0.1", login="monuser", password="password")
    ofile = open(logfile, "ab+")

    writer = csv.writer(ofile, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    while 1:
        result = nut.GetUPSVars("openups")
        global upsData
        param_list = ['ups.status', 'battery.charge', 'battery.current', 'battery.voltage',
                      'battery.temperature', 'input.current', 'input.voltage', 'output.current',
                      'output.voltage']
        output_list = list()
        output_list.append(time.strftime("%Y/%m/%d %H:%M:%S", time.localtime(time.time())))

        for param in param_list:
            upsData[param] = result[param]
            output_list.append(result[param])
        writer.writerow(output_list)
        time.sleep(1.)
    return


def main(argv):
    logfile = argv[0]

    d = threading.Thread(target=pollUps, args=(logfile,))
    d.setDaemon(True)
    d.start()

    logging.info('Started polling thread')

    address = ('0.0.0.0', 5204)
    server = SocketServer.TCPServer(address, EchoRequestHandler)

    d2 = threading.Thread(target=server.serve_forever)
    d2.setDaemon(True)
    d2.start()

    logging.info("Both threads started. Press enter")

    while 1:
        time.sleep(5)


if __name__ == '__main__':
    main(sys.argv[1:])
