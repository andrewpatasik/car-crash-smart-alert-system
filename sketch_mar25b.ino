#include <Wire.h>
#include <PubSubClient.h>
#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include <TinyGPS++.h>

//Define GPS object
TinyGPSPlus gps;

//Define Wifi info
const char* ssid = "Annakost"; //WIFI Name, WeMo will only connect to a 2.4GHz network.
const char* password = "eyang123"; //WIFI Password

//Define MQTT Broker Server
const char* mqtt_test_server = "192.168.0.106";
//const char* mqtt_main_server = "";
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

//For IP Static
/*
IPAddress ip(192, 168, 0, 101); // where xx is the desired IP Address
IPAddress gateway(192, 168, 0, 1); // set gateway to match your network
IPAddress subnet(255, 255, 255, 0); // set subnet mask to match your network
*/

//Define Sensor variable
int16_t accelX, accelY, accelZ;
float gForceX, gForceY, gForceZ;
char buff[100];
//long gyroX, gyroY, gyroZ;
//float rotX, rotY, rotZ;

void setup() {
  Serial.begin(9600);    
  Wire.begin();
  setupWifi();
  WiFi.mode(WIFI_STA);
  setupMPU();
  mqttClient.setServer(mqtt_test_server, 1883);
  reconnect();
  Serial.flush();
}

void loop() {
  recordAccelRegisters();
  //recordGyroRegisters();
  
  while(Serial.available())//While there are characters to come from the GPS
  {
    gps.encode(Serial.read());//This feeds the serial NMEA data into the library one char at a time
  }
  monitorAccelData(); 
//  if(gForceX == -4.00 || gForceX == 4.00 || gForceY == -4.00 || gForceY == 4.00)
//    readAndPublishData();     
}

void setupWifi() {   
  /*
  Serial.print(F("Setting static ip to : "));
  Serial.println(ip);
  */
  
  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  //WiFi.config(ip, gateway, subnet); 
  WiFi.begin(ssid, password);
  
  //Trying to connect it will display dots
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("WiFi connected"); 
  
  delay(500);
}

void setupMPU(){
  // Setup MPU Sensor
  Serial.println("Setting up MPU...");
  Wire.beginTransmission(0b1101000); //This is the I2C address of the MPU (b1101000/b1101001 for AC0 low/high datasheet sec. 9.2)
  Wire.write(0x6B); //Accessing the register 6B - Power Management (Sec. 4.28)
  Wire.write(0b00000000); //Setting SLEEP register to 0. (Required; see Note on p. 9)
  Wire.endTransmission();  
  //Wire.beginTransmission(0b1101000); //I2C address of the MPU
  //Wire.write(0x1B); //Accessing the register 1B - Gyroscope Configuration (Sec. 4.4) 
  //Wire.write(0x00000000); //Setting the gyro to full scale +/- 250deg./s 
  //Wire.endTransmission(); 
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x1C); //Accessing the register 1C - Acccelerometer Configuration (Sec. 4.5) 
  Wire.write(0b00000100); //Setting the accel to +/- 2g
  Serial.println("OK.");
  delay(500);
  Wire.endTransmission(); 
}

void recordAccelRegisters() {
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x3B); //Starting register for Accel Readings
  Wire.endTransmission();
  Wire.requestFrom(0b1101000,6); //Request Accel Registers (3B - 40)
  while(Wire.available() < 6);
  accelX = Wire.read()<<8|Wire.read(); //Store first two bytes into accelX
  accelY = Wire.read()<<8|Wire.read(); //Store middle two bytes into accelY
  accelZ = Wire.read()<<8|Wire.read(); //Store last two bytes into accelZ  

  processAccelData();
}

void processAccelData(){                                                            //
  gForceX = accelX / 8192.0;                                                       //16384.0 = 1g
  gForceY = accelY / 8192.0; 
  gForceZ = accelZ / 8192.0;

}

void monitorAccelData(){                                                            //

    if(gForceX >= -2.72 && gForceX <= 2.36 && gForceY >= -2.82 && gForceY <= 1.82) {
    Serial.print(accelX);
    Serial.print(",");
    Serial.print(accelY);
    Serial.print(",");
    Serial.print(gForceX);
    Serial.print(",");
    Serial.print(gForceY);
    Serial.print(","); 
    Serial.println("Berjalan");
    delay(100);
    } 
    
    else if(gForceX >= -3.78 && gForceX <= 3.68 && gForceY >= -2.79 && gForceY <= 2.75) {
    Serial.print(accelX);
    Serial.print(",");
    Serial.print(accelY);
    Serial.print(",");
    Serial.print(gForceX);
    Serial.print(",");
    Serial.print(gForceY);
    Serial.print(","); 
    Serial.println("Berhenti Mendadak");
    delay(100);
    }
        
    else if(gForceX >= -4.00 || gForceX <= 4.00 || gForceY >= -4.00 || gForceY <= 4.00) {
    Serial.print(accelX);
    Serial.print(",");
    Serial.print(accelY);
    Serial.print(",");
    Serial.print(gForceX);
    Serial.print(",");
    Serial.print(gForceY);
    Serial.print(","); 
    Serial.println("Tabrakan");
    readAndPublishData();     
    delay(100);
    }
    //kasi else

}

/*
void recordGyroRegisters() {
  Wire.beginTransmission(0b1101000); //I2C address of the MPU
  Wire.write(0x43); //Starting register for Gyro Readings
  Wire.endTransmission();
  Wire.requestFrom(0b1101000,6); //Request Gyro Registers (43 - 48)
  while(Wire.available() < 6);
  gyroX = Wire.read()<<8|Wire.read(); //Store first two bytes into accelX
  gyroY = Wire.read()<<8|Wire.read(); //Store middle two bytes into accelY
  gyroZ = Wire.read()<<8|Wire.read(); //Store last two bytes into accelZ
  processGyroData();
}

void processGyroData() {
  rotX = gyroX / 131.0;
  rotY = gyroY / 131.0; 
  rotZ = gyroZ / 131.0;
}
*/

void reconnect() {
  // Loop until reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (mqttClient.connect("wifiClient")) {
      Serial.println("connected");
    } else {
      Serial.print("failed, trying=");
      Serial.print(mqttClient.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void readAndPublishData() {    
  if(mqttClient.connect("wifiClient")) {      
      Serial.println("[ALERT] Accident Occured");
      delay(3000);
      Serial.println("Preparing To Send Data...");
      StaticJsonBuffer<300> JSONbuffer;
      JsonObject& JSONencoder = JSONbuffer.createObject();
      JSONencoder["Device"] = "Wemos D1 R1";
      JsonObject& data = JSONencoder.createNestedObject("data");
      data.set("X-Axis",gForceX);
      data.set("Y-Axis",gForceY);
      data.set("Lat",gps.location.lat(), 6);
      data.set("Long",gps.location.lng(), 6);
      char JSONmessageBuffer[100];
      JSONencoder.printTo(JSONmessageBuffer, sizeof(JSONmessageBuffer));
      Serial.println(JSONmessageBuffer);
      Serial.print("OK...");
      Serial.println();
      delay(3000);
      Serial.println("Publishing Data...");
      mqttClient.publish("finalProject/MPU", JSONmessageBuffer);      
      delay(3000);
      Serial.println("-Sent.");
      reconnect();
      Serial.flush();
    }
  }
