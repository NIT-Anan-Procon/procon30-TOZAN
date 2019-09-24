#include <Wire.h>
#include <Keyboard.h>

// デバイスアドレス(スレーブ)
int E03_DEVICE_ADDRESS = 0x40;
int DISTANCE_ADDRESS = 0x5E;

//ステッパーとセンサの距離ごとの割り当て用
String key = "0";

//ボタンピン設定
const int LbtnPin = 7;
const int RbtnPin = 8;

//1つ前のループのボタン状態保存用
volatile int BLbtnState = HIGH;
volatile int BRbtnState = HIGH;

void setup() {
  Wire.begin();
  
  Keyboard.begin();

  pinMode(LbtnPin, INPUT_PULLUP);
  pinMode(RbtnPin, INPUT_PULLUP);
}

void loop(){
// 距離計算用     　
  int dis[2];

// 距離
  int distance;

// ボタン
  int LbtnState = digitalRead(LbtnPin);
  int RbtnState = digitalRead(RbtnPin);

// 測距センサからの受取設定
  Wire.beginTransmission(E03_DEVICE_ADDRESS);   
  Wire.write(DISTANCE_ADDRESS);
  Wire.endTransmission();
// 測距センサからの読み取り
  Wire.requestFrom(E03_DEVICE_ADDRESS, 2);
  for (int i=0; i< 2; i++){
    while (Wire.available() == 0 ){}
    dis[i] = Wire.read();
  }

// 距離の計算　
  distance = dis[0]<<4;
  distance |= dis[1];
  distance = (distance / 16) / 4;

/* 距離キー設定
 * 28cm以上なら0
 * 23cm以上28cm未満なら1または9
 * 18cm以上23cm未満なら2または8
 * 13cm以上18cm未満なら3または7
 * 8cm以上13cm未満なら4または6
 * 8cm未満なら5を出力
 * 0から9を順番にループ出力
 */
  if(distance >= 28){
    if(key == "9"){
      key = "0";
      Keyboard.print(key);
    }
  }
  if(distance < 28 && distance >= 23){
    if(key == "0"){
      key = "1";
      Keyboard.print(key);
    }
    else if(key == "8"){
      key = "9";
      Keyboard.print(key);
    }
  }
  if(distance < 23 && distance >= 18){
    if(key == "1"){
      key = "2";
      Keyboard.print(key);
    }
    else if(key == "7"){
      key = "8";
      Keyboard.print(key);
    }
  }
  if(distance < 18 && distance >= 13){
    if(key == "2"){
      key = "3";
      Keyboard.print(key);
    }
    else if(key == "6"){
      key = "7";
      Keyboard.print(key);
    }
  }
  if(distance < 13 && distance >= 8){
    if(key == "3"){
      key = "4";
      Keyboard.print(key);
    }
    else if(key == "5"){
      key = "6";
      Keyboard.print(key);
    }
  }
  if(distance < 8){
    if(key == "4"){
      key = "5";
      Keyboard.print(key);
    }
  }

// ボタンキー設定
  if(BLbtnState == HIGH){
    if(LbtnState == LOW){
      Keyboard.print("L");
    }
  }
  if(BRbtnState == HIGH){
    if(RbtnState == LOW){
      Keyboard.print("R");
    }
  }

// ボタンの状態の書き換え
  BLbtnState = LbtnState;
  BRbtnState = RbtnState;
  
  delay(50);
  
}