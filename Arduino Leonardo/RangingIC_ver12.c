  #include <Wire.h>
  #include <Keyboard.h>
  
  // デバイスアドレス(スレーブ)
  int E03_DEVICE_ADDRESS = 0x40;
  int DISTANCE_ADDRESS = 0x5E;
  
  //ステッパーとセンサの距離ごとの割り当て用
  String key = "0";
  
  //ボタンピン設定
  const int LbtnPin = 7;
  const int RbtnPin = 5;
  
  //1つ前のループのボタン状態保存用
  volatile int BLbtnState = HIGH;
  volatile int BRbtnState = HIGH;
  
  int dist_step_old=9;  //距離の6段階1つ前の値9で初期化
  int dist_max=25;      //距離の最大値保存変数25で初期化
  int dist_min=10;      //距離の最小値保存変数15で初期化
  
  
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
    for (int i=0; i< 2; i++)   {
      while (Wire.available() == 0 ){}
      dis[i] = Wire.read();
    }
  
  // 距離の計算　
    distance = dis[0]<<4;
    distance |= dis[1];
    distance = (distance / 16) / 4;
  
    Serial.println(distance);
  
  if(distance<dist_min) dist_min=distance; //min値を更新
  if(distance>dist_max && distance<40) dist_max=distance; //max値を更新
   if(distance>40) dist_max=28;
  
    
  
  /* 距離キー設定
  *  (dist_max-dist_min)/10の3,4,4,4,4,3幅を0～5のステップに割り当て
  */
    int distance_step;//距離の6段階保存変数
    for(distance_step=0;distance_step<=5;distance_step++){
      if(distance < (distance_step*1+2)*(dist_max-dist_min)/8+dist_min)break;         
    }
    
    if(distance_step != dist_step_old){//前回送信ステップと異なる時のみ出力
      switch(distance_step){
        case 0:
        key = "0";
          break;
        case 1:
        key = "1";
          break;
        case 2:
        key = "2";
          break;
        case 3:
        key = "3";
          break;
        case 4:
        key = "4";
          break;
        case 5:
        key = "5";
          break;
      }
          Keyboard.print(key);
      dist_step_old=distance_step; //送信ステップの保存
    }
  
  // ボタンキー設定
    if(BLbtnState == HIGH){
      if(LbtnState == LOW){
        Keyboard.print("l");
      }
    }
    if(BRbtnState == HIGH){
      if(RbtnState == LOW){
        Keyboard.print("r");
      }
    }
  
  // ボタンの状態の書き換え
    BLbtnState = LbtnState;
    BRbtnState = RbtnState;
    
    delay(100);
    
  }