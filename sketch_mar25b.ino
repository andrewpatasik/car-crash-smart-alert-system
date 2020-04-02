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

long lastMsg = 0;
char msg[50];
int value = 0;

//For IP Static
/*
IPAddress ip(192, 168, 0, 112); // where xx is the desired IP Address
IPAddress gateway(192, 168, 0, 1); // set gateway to match your network
IPAddress subnet(255, 255, 255, 0); // set subnet mask to match your network
*/

//Define Sensor variable
int16_t accelX, accelY, accelZ;
float gForceX, gForceY, gForceZ;

//long gyroX, gyroY, gyroZ;
//float rotX, rotY, rotZ;

void setup() {
  Serial.begin(9600);    
  Wire.begin();
  setupWifi();
  setupMPU();
  mqttClient.setServer(mqtt_test_server, 1883);
//client.setServer(mqtt_main_server, 1883);
//client.setCallback(callback);  
  reconnect();
}

void loop() {
  recordAccelRegisters();
  //recordGyroRegisters();
  delay(10);  
   
  readAndPublishData();   
  //while (Serial.available() > 0)
    //if (gps.encode(Serial.read()))
       //displayInfo();      
    
delay(100);
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
  Wire.write(0b00000000); //Setting the accel to +/- 2g
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

void processAccelData(){
  gForceX = accelX / 16384.0;
  gForceY = accelY / 16384.0; 
  gForceZ = accelZ / 16384.0;
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
/*
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  // Switch on the LED if an 1 was received as first character
  if ((char)payload[0] == '1') {
    digitalWrite(BUILTIN_LED, LOW);   // Turn the LED on (Note that LOW is the voltage level
    // but actually the LED is on; this is because
    // it is acive low on the ESP-01)
  } else {
    digitalWrite(BUILTIN_LED, HIGH);  // Turn the LED off by making the voltage HIGH
  }

}
*/
void reconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (mqttClient.connect("wifiClient")) {
      Serial.println("connected");
      mqttClient.publish("finalProject/MPU", "Listening.");      
      // ... and resubscribe
      mqttClient.subscribe("finalProject/MPU");
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
     
    if( gForceX > 0.50 || gForceX < -0.50 ) {
      Serial.println("[TEST] Rear-Side Accident Occured");
      delay(1000);
      Serial.println("Get Accelerometer Data...");
      StaticJsonBuffer<300> JSONbuffer;
      JsonObject& JSONencoder = JSONbuffer.createObject();
      JSONencoder["Device"] = "Wemos D1 R1";
      JsonObject& data = JSONencoder.createNestedObject("data");
      data.set("X-Axis",gForceX);
      data.set("Y-Axis",gForceY);
      data.set("Lat","-0.00");
      data.set("Long","-0.00");
      char JSONmessageBuffer[100];
      JSONencoder.printTo(JSONmessageBuffer, sizeof(JSONmessageBuffer));
      Serial.println(JSONmessageBuffer);
      delay(1000);
      Serial.println("Publishing Data...");
      mqttClient.publish("finalProject/MPU", JSONmessageBuffer);      
      delay(1000);
      Serial.println("-Sent.");
    } else if( gForceY > 0.50 || gForceY < -0.50 ) {
      Serial.println("[TEST] Front-Side Accident Occured");
      delay(1000);
      Serial.println("Get Accelerometer Data...");
      StaticJsonBuffer<300> JSONbuffer;
      JsonObject& JSONencoder = JSONbuffer.createObject();
      JSONencoder["Device"] = "Wemos D1 R1";
      JsonObject& data = JSONencoder.createNestedObject("data");
      data.set("X-Axis",gForceX);
      data.set("Y-Axis",gForceY);
      data.set("Lat","-0.00");
      data.set("Long","-0.00");
      char JSONmessageBuffer[100];
      JSONencoder.printTo(JSONmessageBuffer, sizeof(JSONmessageBuffer));
      Serial.println(JSONmessageBuffer);
      delay(1000);
      Serial.println("Publishing Data...");
      mqttClient.publish("finalProject/MPU", JSONmessageBuffer);      
      delay(1000);
      Serial.println("-Sent.");    }
  }

/*
  if(client.connected()) {
  if (gForceX > 0.50 || gForceX < -0.50 || gForceY > 0.50 || gForceY < -0.50) {
  while (Serial.available() > 0)
    if (gps.encode(Serial.read()))
      displayInfo();
  }
  delay(100);
  */
}
/*
void readAndPublishData() {    
  if(mqttClient.connect("wifiClient")) {    
     
    if( gForceX > 0.50 || gForceX < -0.50 ) {
      Serial.println("[TEST] Accident Occured");
      delay(1000);
      Serial.println("Get Accelerometer Data...");
      String getSensorData = String(gForceX) + "," + String(gForceY) + "," + "Rear-Side Crash ";
      char sensorDataAsCharAway[getSensorData.length()];
      getSensorData.toCharArray(sensorDataAsCharAway, getSensorData.length());
      Serial.println(getSensorData);
      delay(1000);
      Serial.println("Publishing Data...");
      mqttClient.publish("finalProject/MPU", sensorDataAsCharAway);      
      delay(1000);
      Serial.println("-Sent.");
    } else if( gForceY > 0.50 || gForceY < -0.50 ) {
      Serial.println("[TEST] Accident Occured");
      delay(1000);
      Serial.println("Get Accelerometer Data...");
      String getSensorData = String(gForceX) + "," + String(gForceY) + "," + "Front-Side Crash ";
      char sensorDataAsCharAway[getSensorData.length()];
      getSensorData.toCharArray(sensorDataAsCharAway, getSensorData.length());
      Serial.println(getSensorData);
      delay(1000);
      Serial.println("Publishing Data...");
      mqttClient.publish("finalProject/MPU", sensorDataAsCharAway);
      delay(1000);
      Serial.println("-Sent.");    }
  }
*/

void getCoordinate()
{
  Serial.print(F("Location: ")); 
  if (gps.location.isValid())
  {
    Serial.print(gps.location.lat(), 6);
    Serial.print(F(","));
    Serial.print(gps.location.lng(), 6);
  }
  else
  {
    Serial.print(F("INVALID"));
  }
/*
  Serial.print(F("  Date/Time: "));
  if (gps.date.isValid())
  {
    Serial.print(gps.date.month());
    Serial.print(F("/"));
    Serial.print(gps.date.day());
    Serial.print(F("/"));
    Serial.print(gps.date.year());
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.print(F(" "));
  if (gps.time.isValid())
  {
    if (gps.time.hour() < 10) Serial.print(F("0"));
    Serial.print(gps.time.hour());
    Serial.print(F(":"));
    if (gps.time.minute() < 10) Serial.print(F("0"));
    Serial.print(gps.time.minute());
    Serial.print(F(":"));
    if (gps.time.second() < 10) Serial.print(F("0"));
    Serial.print(gps.time.second());
    Serial.print(F("."));
    if (gps.time.centisecond() < 10) Serial.print(F("0"));
    Serial.print(gps.time.centisecond());
  }
  else
  {
    Serial.print(F("INVALID"));
  }

  Serial.println();
*/
}

