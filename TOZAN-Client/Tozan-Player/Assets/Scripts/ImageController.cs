using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ImageController : MonoBehaviour
{

    private float r, g, b;
    //[SerializeField] float sample = 1.0f;

    private void Update()
    {
        //SetAlpha(sample);
    }

    //画像ファイルの読み込みを行って、全画面表示される大きさする。
    public void SetImageFile(string filename)
    {
        GetComponent<Image>().sprite = Resources.Load<Sprite>(filename);
        GetComponent<RectTransform>().offsetMin = new Vector2(0, 0);
        GetComponent<RectTransform>().offsetMax = new Vector2(0, 0);
    }

    public void SetAlpha(float a)
    {
        this.r = GetComponent<Image>().color.r;
        this.g = GetComponent<Image>().color.g;
        this.b = GetComponent<Image>().color.b;
        GetComponent<Image>().color = new Color(r,g,b,a);
    }
    
}
