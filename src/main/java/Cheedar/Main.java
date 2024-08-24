package Cheedar;

/*
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalInputProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalOutputProvider;
import com.pi4j.plugin.pigpio.provider.pwm.PiGpioPwmProvider;
import com.pi4j.plugin.pigpio.provider.serial.PiGpioSerialProvider;
import com.pi4j.plugin.pigpio.provider.spi.PiGpioSpiProvider;
import com.pi4j.plugin.raspberrypi.platform.RaspberryPiPlatform;
*/
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/*TODO:
 * LCD output is temporarily disabled. There currently is no need for a physical display.
 * Hopefully in the future I can buy or make a large 7 segment display to display the speeds.
 */

public class Main {
    public static void main(String[] args) {
        /*
        //this initializes Pi4J and the I2C LCD
        PiGpio piGpio = PiGpio.newNativeInstance();
        Context pi4j = Pi4J.newContextBuilder()
                .noAutoDetect()
                .add(new RaspberryPiPlatform() {
                    @Override protected String[] getProviders() { return new String[]{}; }})
                .add(PiGpioDigitalInputProvider.newInstance(piGpio),
                        PiGpioDigitalOutputProvider.newInstance(piGpio),
                        PiGpioPwmProvider.newInstance(piGpio),
                        PiGpioSerialProvider.newInstance(piGpio),
                        PiGpioSpiProvider.newInstance(piGpio),
                        LinuxFsI2CProvider.newInstance()
                )
                .build();
        //create a component, with amount of rows and columns of the LCD
        //note, this uses an I2C LCD, not the kind that has a billion pins.
        LcdDisplay lcd = new LcdDisplay(pi4j, 4, 16);
        lcd.setDisplayBacklight(true);
        */
        String[] result = SerialPortList.getPortNames();
        while (result.length == 0) {
            System.out.println("No ports detected. Waiting 5 seconds.");
            //lcd.displayText("No USB Radar detected.\nWaiting 5 seconds.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = SerialPortList.getPortNames();
        }
        SerialPort radarPort = new SerialPort(result[0]);
        //note, this chooses the first serial port (USB port) available to be used as the radar port.
        //make sure you actually have the radar plugged in too
        try {
            radarPort.openPort();
            radarPort.setParams(1000000, 8, 1, 0);
            radarPort.setRTS(false);
            Thread.sleep(100);      //allow the radar to boot up and start running its code
            if (radarPort.getInputBufferBytesCount() > 5) {
                byte[] versionBytes = radarPort.readBytes(radarPort.getInputBufferBytesCount());
                String version = null;
                for (int i = 0; i < versionBytes.length; i++) {
                    if (versionBytes[i] == 'C') {
                        version = new String(versionBytes, i, versionBytes.length - 2);
                        break;
                    }
                }
                System.out.println(version);
                //version output is "CDM324 fw v0.1, compiled Aug 15 2023 23:41:17"
                //the code above basically grabs the output at "C" in "CDM" and lines it up after that in case of
                //the version information coming in out of order at boot up (partially sent, cut off, etc.)
            }
            //lcd.clearDisplay();
            //lcd.displayText("Speed MPH:",2);
            float highSpeed = 0.0f;
            long lastResetTime = System.currentTimeMillis();
            final long RESET_INTERVAL = 30000;      //30 second reset time.
            while (radarPort.isOpened()) {
                radarPort.writeByte((byte) 'm');   //change this to 'k' for kilometers per hour
                Thread.sleep(100);              //this is needed to let the radar actually compute the info it needs
                String s = radarPort.readString(); //read the data that is now available from the radar.
                if (s != null) {
                    float speed = Float.parseFloat(s.trim()) / 10;
                    if (speed > 25.0f) {
                        System.out.println("HIGH SPEED!!! [" + speed + "]");
                        if (speed > highSpeed) {
                            highSpeed = speed;
                        }
                    }
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastResetTime >= RESET_INTERVAL) {
                        System.out.println("Resetting top speed tracking - highest recorded speed: " + highSpeed);
                        if (highSpeed > 25.0f) {
                            float finalHighSpeed = highSpeed;
                            Thread logSpeed = new Thread(() -> {
                                Logger instance = new Logger();
                                instance.logSpeed(finalHighSpeed);
                            });
                            logSpeed.start();
                        }
                        highSpeed = 0.0f;
                        lastResetTime = currentTime;
                    }
                    System.out.print(speed + "\r");
                    //lcd.displayText(String.valueOf(speed),1);
                }
            }
        } catch (SerialPortException ex) {
            System.out.println("Error opening port:\n" + ex);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}