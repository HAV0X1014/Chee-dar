# Chee-dar
A project using [limpkin's CDM324 radar](https://github.com/limpkin/cdm324_fft), a Raspberry Pi Zero W, and I2C LCD display (potentially a different display in the future) to track the speed of running Cheetahs. The purpose of this project is to be as low cost as possible.

This uses Pi4J, some code borrowed from [txemaromero](https://github.com/txemaromero/lcddisplay-java) for the I2C LCD display, and jssc to interface with the serial connection to the radar.

## Usage
Install Raspberry Pi OS minimal 32 bit, Pi4J, Java 17, and wiringpi onto the Pi Zero. Connect the CDM324 - backpack and expansion board - over USB to the Raspberry Pi. Wire the I2C LCD to the Raspberry Pi's GPIO pins. Run the compiled Jar with `java -jar Chee-dar-x.x-all.jar`.
