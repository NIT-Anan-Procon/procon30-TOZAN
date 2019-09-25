using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public static class Parameter
{
    public static int MIN_VAL = 0;
    public static int MAX_VAL = 9;

    public static string IMAGE_PREFAB_FILE = "Prefabs/Image";

    //キー入力された数字をINTで取得
    public static int GetInputNumber()
    {
        if (Input.GetKeyDown("0")) return 0;
        if (Input.GetKeyDown("1") || Input.GetKeyDown("9")) return 1;
        if (Input.GetKeyDown("2") || Input.GetKeyDown("8")) return 2;
        if (Input.GetKeyDown("3") || Input.GetKeyDown("7")) return 3;
        if (Input.GetKeyDown("4") || Input.GetKeyDown("6")) return 4;
        if (Input.GetKeyDown("5")) return 5;
        //if (Input.GetKeyDown("6")) return 6;
        //if (Input.GetKeyDown("7")) return 7;
        //if (Input.GetKeyDown("8")) return 8;
        //if (Input.GetKeyDown("9")) return 9;
        return -1;
    }

    //キー入力された文字列を取得 (-1:キー入力なし | 0:"L" | 1:"R")
    public static int GetInputString()
    {
        if (Input.GetKeyDown("L")) return 0;
        if (Input.GetKeyDown("R")) return 1;
        return -1;
    }
}
