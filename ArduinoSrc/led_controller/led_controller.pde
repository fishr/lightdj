#include <SoftwareSerial.h>


int CHANNEL_PWM_PINS[] = {3, 5, 6, 9, 10, 11};
#define NUM_CHANNELS 6
#define SERIAL_SPEED 115200
#define LCD_PIN 2
#define CHARS_PER_LINE 16
#define DELAY_SW_SERIAL 500

byte channelVals[] = {0, 0, 0, 0, 0, 0};
byte currentByte = 0;
int channelIndex = 0;

// Keep ASCII strings for the serial
char topLine[CHARS_PER_LINE];
char bottomLine[CHARS_PER_LINE];
int charIndexTop = 0;
int charIndexBottom = 0;

// Set up a software serial to control the LCD
SoftwareSerial lcdSerial = SoftwareSerial(0, LCD_PIN);

// The state: 	0 => PWM channel data
//		1 => Text data for the LCD screen top line
//		2 => Text data for the LCD screen bottom line
int state = 0;

// Accepts consecutive serial commands in the form: C0,83,210R.
//	The C indicates to reset to start a new frame
//	The values are base-10 values, in the range of 0-255 inclusive
//	Commas separate values
//	The final R indicates the end, and publishes the values (in this case, sending them out as PWM), and restarts

void setup() {
	for(int i = 0; i < NUM_CHANNELS; i++) {
		pinMode(CHANNEL_PWM_PINS[i], OUTPUT);	
	}
	Serial.begin(SERIAL_SPEED);

	// Set up the LCD
	pinMode(LCD_PIN, OUTPUT);
	lcdSerial.begin(9600);
	backlightOff();
	clearDisplay();
	selectLineOne();
	lcdSerial.print(topLine);
	selectLineTwo();
	lcdSerial.print(bottomLine);
}


void loop() {
	// Attempt to read the byte from the serial port
	if (Serial.available()) {
		// Process the byte
		byte b = Serial.read();
		
		if (b == 'C' && state == 0) { 
			// Start of a new frame
			currentByte = 0;
			channelIndex = 0;
			state = 0;

		} else if (b == ',') {
			// Separator between values. Store this one and get ready for the next one.
			channelVals[channelIndex] = currentByte;
			channelIndex = (channelIndex + 1) % NUM_CHANNELS;
			currentByte = 0;

		} else if (b == 'R' && state == 0) {
			// The render directive. Store the current byte and render. Go back to the beginning
			channelVals[channelIndex] = currentByte;
			channelIndex = 0;
			currentByte = 0;

			// Render!
			for(int i = 0; i < NUM_CHANNELS; i++) {
				pwmLight(channelVals[i], CHANNEL_PWM_PINS[i]);
			}

		} else if (b == '<') {
			// Change state to record to the top line
			charIndexTop = 0;
			state = 1;
			for(int i = 0; i < CHARS_PER_LINE; i++) {
				topLine[i] = 0;
			}

		} else if (b == '>') {
			// Change state to record to the bottom line
			charIndexBottom = 0;
			state = 2;
			for(int i = 0; i < CHARS_PER_LINE; i++) {
				bottomLine[i] = 0;
			}

		} else if (b == '#') {
			// Send data to the LCD screen
			state = 0;
			clearDisplay();
			selectLineOne();
			for(int i = 0; i < charIndexTop; i++) {
				lcdSerial.print(topLine[i]);
				delay(DELAY_SW_SERIAL);
			}			
		

			selectLineTwo();
			for(int i = 0; i < charIndexBottom; i++) {
				lcdSerial.print(bottomLine[i]);
				delay(DELAY_SW_SERIAL);
			}

		} else if (b == '[') {
			backlightOn();

		} else if (b == ']') {
			backlightOff();

		} else {
			if (state == 0) {			
				// It's probably a value
				byte digit = b - '0';
				currentByte = 10*currentByte + digit;
			} else if (state == 1) {
				topLine[charIndexTop] = b;
				charIndexTop = (charIndexTop + 1) % CHARS_PER_LINE;
			} else if (state == 2) {
				bottomLine[charIndexBottom] = b;
				charIndexBottom = (charIndexBottom + 1) % CHARS_PER_LINE;
			}
		}

		

	}
}

void pwmLight(byte b, int channel) {
	
	float p = b / 255.0;
	float v = pow(2.0, 8*p) - 1.0; // Account for the non-linear current to light ratio of the LED's

	int pwm = (int) (v + 0.5);
	pwm = constrain(pwm, 0, 255);

	analogWrite(channel, pwm);

}



void backlightOn() {
  lcdSerial.print(124, BYTE);
  delay(DELAY_SW_SERIAL);
  lcdSerial.print(157, BYTE);
  delay(DELAY_SW_SERIAL);
}

void backlightOff() {
  lcdSerial.print(124, BYTE);
  delay(DELAY_SW_SERIAL);
  lcdSerial.print(128, BYTE);
  delay(DELAY_SW_SERIAL);
}

void clearDisplay() {
  lcdSerial.print(0xFE, BYTE);
  delay(DELAY_SW_SERIAL);
  lcdSerial.print(0x01, BYTE); 
  delay(DELAY_SW_SERIAL);
}

void selectLineOne() {
  lcdSerial.print(0xFE, BYTE); 
  delay(DELAY_SW_SERIAL);
  lcdSerial.print(128, BYTE);
  delay(DELAY_SW_SERIAL);
}

void selectLineTwo() {
  lcdSerial.print(0xFE, BYTE);
  delay(DELAY_SW_SERIAL);
  lcdSerial.print(192, BYTE); 
  delay(DELAY_SW_SERIAL);
}
