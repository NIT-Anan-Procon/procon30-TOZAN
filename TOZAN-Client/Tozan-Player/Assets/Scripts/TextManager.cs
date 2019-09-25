using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class TextManager : MonoBehaviour
{ 
    [SerializeField] Text stepsText;
    [SerializeField] Text timeText;
    [SerializeField] Text calText;
    float time;

    // Start is called before the first frame update
    void Start()
    {
        time = 0.0f;
    }

    // Update is called once per frame
    void Update()
    {
        stepsText.text = "歩数 : " + GetComponent<EventManager>().steps +"歩";
        time += Time.deltaTime;
        int s = (int)time % 60;
        int m = (int)(time / 60);
        timeText.text = "経過時間 "+(m<10? "0":"") + m + (s<10? ":0" : ":") + s;
        calText.text = "消費カロリー "+(int)(GetComponent<EventManager>().steps * 0.1395f)+"kcal";
    }
}
