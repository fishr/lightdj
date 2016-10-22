# lightDJ
Next Make's lightDJ controller

Welcome to lightDJ, a Java-based programming for controlling a few different hardware versions of party lighting systems.  This was originally written by @sjlevine.

In general, it takes incoming sound, processes it via FFT, and generates lighting values from this sono-temporal data.  It then compresses the data and, in the version developed for the Next Make wired lights, sends it out a USB serial port to an RS-232 to RS-485 converter to transmit the data to all the lights.

Currently supported by <ryanfishme@gmail.com>
