using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System.Xml.Linq;
using System.IO;

public class EventManager : MonoBehaviour
{
    GameObject imagePrefab;
    GameObject frontImage, backImage;

    int backIndex;

    List<string> filenames = new List<string>();

    bool setup = false;
    int nextTarget = 5;
    public int steps = 0;
    private readonly int CHANGE_STEP = 4;


    
    string stageID = "11111111";


    int num = -1, formerNum = -2;
    int str = -1, formerStr = -2;

    void Start()
    {

        LoadXML();

        imagePrefab = (GameObject)Resources.Load("Prefabs/Image");
        backIndex = 1;

        backImage = (GameObject)Instantiate(imagePrefab, new Vector3(), Quaternion.identity, GameObject.Find("Images").transform);
        backImage.GetComponent<ImageController>().SetImageFile("Stages/"+stageID+"/n_view/"+ GetPathWithoutExtension(filenames[backIndex]));

        frontImage = (GameObject)Instantiate(imagePrefab, new Vector3(), Quaternion.identity, GameObject.Find("Images").transform);
        frontImage.GetComponent<ImageController>().SetImageFile("Stages/" + stageID + "/n_view/" + GetPathWithoutExtension(filenames[backIndex-1]));
        
    }

    // Update is called once per frame
    void Update()
    {
        num = GetInputNumber();
        str = GetInputString();

        if (str == 0) Debug.Log("L");
        if (str == 1) Debug.Log("R");




        if (num == 0) setup = true;

        if (setup && (num != formerNum) && (num > -1))
        {

            if (num == nextTarget)
            {
                steps++;
                if (steps % CHANGE_STEP == 0)
                {
                    backIndex++;

                    GameObject temp1 = frontImage;
                    Destroy(temp1);
                    GameObject temp2 = backImage;
                    Destroy(temp2);

                    backImage = (GameObject)Instantiate(imagePrefab, new Vector3(), Quaternion.identity, GameObject.Find("Images").transform);
                    backImage.GetComponent<ImageController>().SetImageFile("Stages/" + stageID + "/n_view/" + GetPathWithoutExtension(filenames[backIndex]));

                    frontImage = (GameObject)Instantiate(imagePrefab, new Vector3(), Quaternion.identity, GameObject.Find("Images").transform);
                    frontImage.GetComponent<ImageController>().SetImageFile("Stages/" + stageID + "/n_view/" + GetPathWithoutExtension(filenames[backIndex - 1]));

                    Debug.Log("backIndex:" + backIndex);
                }
                nextTarget = (nextTarget == 0 ? 5 : 0);
                
                Debug.Log("steps:" + steps.ToString());
            }

            Debug.Log((float)((nextTarget == 0 ? (5 - num) : num) + (steps % CHANGE_STEP) * 5) / (float)(CHANGE_STEP * 5));
            frontImage.GetComponent<ImageController>().SetAlpha(1.0f - ((float)((nextTarget == 0 ? (5 - num) : num) + (steps % CHANGE_STEP) * 5) / (float)(CHANGE_STEP * 5)));
        }

        formerNum = num;
        formerStr = str;
    }

    //キー入力された数字をINTで取得 (-1:キー入力なし)
    int GetInputNumber()
    {
        if(Input.GetKeyDown("0")) return 0;
        if (Input.GetKeyDown("1") || Input.GetKeyDown("9")) return 1;
        if (Input.GetKeyDown("2") || Input.GetKeyDown("8")) return 2;
        if (Input.GetKeyDown("3") || Input.GetKeyDown("7")) return 3;
        if (Input.GetKeyDown("4") || Input.GetKeyDown("6")) return 4;
        if (Input.GetKeyDown("5")) return 5;
        return -1;
    }

    //キー入力された文字列を取得 (-1:キー入力なし | 0:"L" | 1:"R")
    int GetInputString()
    {
        if (Input.GetKeyDown("l")) return 0;
        if (Input.GetKeyDown("r")) return 1;
        return -1;
    }

    void LoadXML()
    {
        XDocument xml = XDocument.Load(Application.dataPath + "/Resources/Stages/" + stageID + "/meta.xml");

        XElement tozan = xml.Element("TOZAN");

        XElement resources = tozan.Element("resources");

        XElement normal = resources.Element("normal");

        var images = normal.Elements("image");

        foreach (XElement image in images)
        {
            filenames.Add(image.Value);
        }
    }

    string GetPathWithoutExtension(string path)
    {
        var extension = Path.GetExtension(path);
        if (string.IsNullOrEmpty(extension))
        {
            return path;
        }
        return path.Replace(extension, string.Empty);
    }
}