#include <SoftwareSerial.h>

#include "Tlc5940.h"

#define NUM_CHANNELS 3
#define SERIAL_SPEED 115200

int channelVals[] = {0, 0, 0};
int currentVal = 0;
int channelIndex = 0;


// Accepts consecutive serial commands in the form: C0,83,210R.
//	The C indicates to reset to start a new frame
//	The values are base-10 values, in the range of 0-4095 inclusive  
//	Commas separate values
//	The final R indicates the end, and publishes the values (in this case, sending them out to the TLC), and restarts

void setup() {
	Tlc.init();
	Serial.begin(SERIAL_SPEED);
}


void loop() {
	// Attempt to read the byte from the serial port
	if (Serial.available()) {
		// Process the byte
		byte b = Serial.read();
		
		if (b == 'C') { 
			// Start of a new frame
			currentVal = 0;
			channelIndex = 0;

		} else if (b == ',') {
			// Separator between values. Store this one and get ready for the next one.
			channelVals[channelIndex] = currentVal;
			channelIndex = (channelIndex + 1) % NUM_CHANNELS;
			currentVal = 0;

		} else if (b == 'R') {
			// The render directive. Store the current byte and render. Go back to the beginning
			channelVals[channelIndex] = currentVal;
			channelIndex = 0;
			currentVal = 0;

			// Render!
                        Tlc.clear();
			for(int i = 0; i < NUM_CHANNELS; i++) {
				Tlc.set(i, channelVals[i]);
                                //Tlc.set(i + 3, channelVals[i]);
			}
                        Tlc.update();


		} else {
			byte digit = b - '0';
			currentVal = 10*currentVal + digit;
		}

		

	}
}

void pwmLight(byte b, int channel) {
	
	float p = b / 255.0;
	float v = pow(2.0, 8*p) - 1.0; // Account for the non-linear current to light ratio of the LED's

	int pwm = (int) (v + 0.5);
	pwm = constrain(pwm, 0, 255);

	//analogWrite(channel, pwm);

}


