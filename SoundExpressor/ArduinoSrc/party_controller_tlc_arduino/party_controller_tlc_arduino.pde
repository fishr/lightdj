// These settings should be changed.
#define BOARD_INDEX 0
#define SERIAL_SPEED 115200
int tlc_channel_map[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}; // Map R1 G1 B1 R2 G2 B2 ... R4 G4 B4 to the TLC channel equivalents 
#define NUM_LEDS 4
#define CHANNELS_PER_LED 3
#define BYTES_PER_CHANNEL 2

// Protocol information
#define SYNC_BYTE 255
#define ACTION_EMERGENCY_LIGHTING 254
#define ACTION_ALL_OFF 253
#define ACTION_FRONT_LEDS_SAME 252
#define ACTION_REAR_LEDS_SAME 251
#define ACTION_SET_ALL_UVS 250
#define ACTION_SET_ALL_WHITES 249
#define ACTION_STROBE_WHITE 248
#define ACTION_FRONT_PANELS_SAME 247
#define ACTION_REAR_PANELS_SAME 246
#define ACTION_STROBE_UV 245
#define STROBE_TIME_MICROS 100

// State information
#define STATE_IDLE 0
#define STATE_RECEIVING_CMD 1
#define STATE_RECEIVING_DATA 2
#define STATE_STROBE 3
int state = STATE_IDLE;
int command = -1;
int channelIndex = 0;
int byteIndex = 0;
unsigned long strobeStart;
unsigned int currentVal = 0;

// Which board is this?
#define BOARD_FRONT 0
#define BOARD_REAR 1
#define BOARD_STROBE 2
byte boardType;

// Buffer to store current PWM values
int pwm_buffer[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
#define PWM_MAX 4095

#include "Tlc5940.h"

void setup() {
  
  // Set the board type based on the index
  if (BOARD_INDEX < 8) {
    boardType = BOARD_FRONT;
  } else if (BOARD_INDEX < 16) {
    boardType = BOARD_REAR;
  } else {
    boardType = BOARD_STROBE;
  }  
  
  // Reset all state variables
  reset_command();
  
  // Set up hardware to talk to the TLC5940 with hardware support
  Tlc.init();
  
  // Set up hardware serial
  Serial.begin(SERIAL_SPEED);
  
}

void reset_command() {
  state = STATE_IDLE;
  command = -1;
  channelIndex = 0;
  byteIndex = 0;
  currentVal = 0;  
  strobeStart = 0;
}


// Makes sure a value is good
unsigned int limit(unsigned int a) {
  if (a < 0) {
   return 0;
  } else if (a > PWM_MAX) {
   return PWM_MAX;
  } else {
   return a;
  } 
}

// Makes sure all PWM values are good values, and sends to TLC
void dispatch_to_tlc() {
  Tlc.clear();
  for(byte i = 0; i < NUM_LEDS * CHANNELS_PER_LED; i++) {
    Tlc.set(tlc_channel_map[i], limit(pwm_buffer[i]));
  }
  Tlc.update();
  
}


void set_all_pwm(int r, int g, int b) {
  for(byte i = 0; i < NUM_LEDS * CHANNELS_PER_LED; i+= CHANNELS_PER_LED) {
    pwm_buffer[i + 0] = r;
    pwm_buffer[i + 1] = g;
    pwm_buffer[i + 2] = b;
  }
}

void loop() {
  if (Serial.available()) {
    byte b = Serial.read();
    //Serial.write(b);
    
    if (b == SYNC_BYTE) {
      // Just received a sync!
      reset_command();
      state = STATE_RECEIVING_CMD;
      return;
    }
    
    if (state == STATE_RECEIVING_CMD) {
      // We are receiving which command to do
      command = b;
      state = STATE_RECEIVING_DATA;
      
      if (command == ACTION_EMERGENCY_LIGHTING || command == ACTION_ALL_OFF || command == ACTION_STROBE_WHITE || command == ACTION_STROBE_UV) {
        // We should dispatch this command now! Otherwise,
      } else {
        return;  // Dispatch it once we start reading bytes.
      }
    }
    
    if (state == STATE_STROBE) {
      // Release the strobe if it's time yet 
      unsigned long t = micros();
      if (t - strobeStart > STROBE_TIME_MICROS || strobeStart > t) {
        set_all_pwm(0, 0, 0);
        dispatch_to_tlc();
        reset_command();
      }
    }
   
    if (state == STATE_RECEIVING_DATA) {
      // We are receiving data for this command
      switch(command) {
      case ACTION_EMERGENCY_LIGHTING:
        set_all_pwm(PWM_MAX, PWM_MAX, PWM_MAX);
        dispatch_to_tlc();
        reset_command();
        break;
        
      case ACTION_ALL_OFF:
        set_all_pwm(0, 0, 0);
        dispatch_to_tlc();
        reset_command();
        break;
       
      case ACTION_FRONT_LEDS_SAME:  // Read in a color (3 values, and set them)
        // Only if applicable
        if (boardType != BOARD_FRONT) {reset_command();}
        
        currentVal = (currentVal << 8) + b;
        byteIndex++;
          
        if (byteIndex == BYTES_PER_CHANNEL) {
          pwm_buffer[channelIndex++] = currentVal;
          byteIndex = 0;
          currentVal = 0;
            
          if (channelIndex == CHANNELS_PER_LED) {
            // Dispatch
            set_all_pwm(pwm_buffer[0], pwm_buffer[1], pwm_buffer[2]);
            dispatch_to_tlc();
            reset_command();
          }
        }
           
        break;
       
       case ACTION_FRONT_PANELS_SAME:
       case BOARD_INDEX: 
          // Only if applicable
        if (boardType != BOARD_FRONT) {reset_command();}
        
        currentVal = (currentVal << 8) + b;
        byteIndex++;
          
        if (byteIndex == BYTES_PER_CHANNEL) {
          pwm_buffer[channelIndex++] = currentVal;
          byteIndex = 0;
          currentVal = 0;
            
          if (channelIndex == NUM_LEDS * CHANNELS_PER_LED) {
            // Dispatch
            dispatch_to_tlc();
            reset_command();
          }
        }
         break;
         
         
       case ACTION_STROBE_WHITE:
          // Start a strobe!
          set_all_pwm(PWM_MAX, PWM_MAX, PWM_MAX);
          dispatch_to_tlc();
          strobeStart = micros();
          state = STATE_STROBE;
          break;
       
       case ACTION_REAR_PANELS_SAME:
       case ACTION_REAR_LEDS_SAME:
       case ACTION_SET_ALL_UVS:
       case ACTION_SET_ALL_WHITES:
       case ACTION_STROBE_UV:
         // Not implemented
         break;
      }
      
    

      
    } else if (state = STATE_IDLE) {
      // Ignore this byte - it is not relevant to us
      
    }
    
  }
}


