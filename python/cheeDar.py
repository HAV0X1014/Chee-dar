import time
import serial
import glob
import sys
from rpi_lcd import LCD


def checkserialports():
    #this is what actually says "these are the available serial ports"
    result = []
    if sys.platform.startswith('win'):
        ports = ['COM%s' % (i + 1) for i in range(256)]
    elif sys.platform.startswith('linux') or sys.platform.startswith('cygwin'):
        # this excludes your current terminal "/dev/tty"
        ports = glob.glob('/dev/tty[A-Za-z]*')
    elif sys.platform.startswith('darwin'):
        ports = glob.glob('/dev/tty.*')
    else:
        raise EnvironmentError('Unsupported platform')
    for port in ports:
        try:
            s = serial.Serial(port)
            s.close()
            result.append(port)
        except (OSError, serial.SerialException):
            pass
    return result


def main():
    lcd = LCD()
    result = checkserialports()
    while (len(result) == 0):
        print("No radar detected, waiting 5 seconds.")
        lcd.text("No USB detected.", 1)
        time.sleep(5)
        result = checkserialports()
    print(result)

    radarPort = serial.Serial(result[0], 1000000)
    # radarPort.set_buffer_size(rx_size=1000000, tx_size=1000000) #hack only needed for windows
    radarPort.rts = False
    time.sleep(.1)
    if radarPort.in_waiting > 5:
        version = radarPort.read(radarPort.in_waiting)
        for i in range(len(version)):
            if version[i] == ord('C'):
                version = version[i:-2].decode('utf-8')
                break
    print(version)

    lcd.text("Speed MPH:", 2)
    while True:
        radarPort.write(b'm')
        speed = float(radarPort.read_until(b'\n').decode().strip()) / 10
        print(speed, end='\r')
        lcd.text(str(speed), 1)


if __name__ == '__main__':
    main()
