int CHANNEL_DO_PINS[] = {2, 3, 4, 5, 6, 7};
#define NUM_CHANNELS 6
#define SERIAL_SPEED 115200

byte channelVals[] = {0, 0};


void setup() {
	for(int i = 0; i < NUM_CHANNELS; i++) {
		pinMode(CHANNEL_DO_PINS[i], OUTPUT);	
	}
	Serial.begin(SERIAL_SPEED);

}


void loop() {
	// Attempt to read the byte from the serial port
	if (Serial.available()) {
		// Process the byte
		byte b = Serial.read();
                
		for(int i = 0; i < NUM_CHANNELS; i++) {
			digitalWrite(CHANNEL_DO_PINS[i], bitRead(b, i));
		}    

	}
}

