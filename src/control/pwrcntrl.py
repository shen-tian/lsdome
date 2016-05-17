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
import getopt

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


def poll_ups(nut_server):
    nut_login = "monuser"
    nut_password = "password"

    logfile = 'powerlog.csv'

    logging.info('Connecting to nut-server at %s', nut_server)

    nut = PyNUT.PyNUTClient(host=nut_server, login=nut_login, password=nut_password)
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
        time.sleep(30.)
    return


def usage():
    print 'oops'
    return


def main(argv):

    nut_server = '127.0.0.1'

    try:
        opts, args = getopt.getopt(argv, 'h:u:p', ['host=', 'user=', 'pass='])
    except getopt.GetoptError:
        usage()
        sys.exit(2)

    for opt, arg in opts:
        if opt in ('-h', '-host'):
            nut_server = arg
        else:
            usage()
            sys.exit(2)

    logging.info('Starting power controller.')

    logging.info('Starting polling thread')

    d = threading.Thread(target=poll_ups, args=(nut_server,))
    d.setDaemon(True)
    d.start()

    logging.info('Starting server')

    address = ('0.0.0.0', 5204)
    server = SocketServer.TCPServer(address, EchoRequestHandler)

    server.serve_forever()
    return


if __name__ == '__main__':
    main(sys.argv[1:])
