using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Navigation;
using System.Xml;

namespace TOZAN_Client
{
    class Launch
    {
        public List<string> normal = new List<string>();
        public List<string> expansion = new List<string>();
        public List<string> ads = new List<string>();
        public List<string> errorCount = new List<string>();

        public void FileCheck(string id)
        {
            var metaXml = new XmlDocument();
            metaXml.Load("./data/games/" + id + "/meta.xml");
            var NormalNode = metaXml.SelectNodes("TOZAN/resources/normal/image");
            foreach (XmlNode images in NormalNode)
            {
                try
                {
                    if (File.Exists("./data/games/" + id + "/resources/n_view/" + images.InnerText))
                    {
                        normal.Add(images.InnerText);
                    }
                    else
                    {
                        errorCount.Add(id);
                        continue;
                    }
                }
                catch(NullReferenceException)
                {
                    errorCount.Add(id);
                    continue;
                }
            }
            /*
            var ExpNode = metaXml.SelectNodes("TOZAN/resources/expansion/image");
            foreach (XmlNode image in ExpNode)
            {
                try
                {
                    if (File.Exists("./data/games/" + name + "/resources/s_view/" + image.InnerText))
                    {
                        normal.Add(image.InnerText);
                    }
                }
                catch (NullReferenceException)
                {
                    errorCount.Add(name);
                    continue;
                }
            }
            */
        }
        public void UnZip(string ID)
        {
            //Zip解凍
            string input = @".\data\temp\" + ID + ".zip";
            string output = @".\data\games\" + ID;
            using (var zip = new Ionic.Zip.ZipFile(input, Encoding.GetEncoding("utf-8")))
            zip.Dispose();
            return;
        }

        public void EditPageMove(NavigationService navi)
        {
            EditPage edit = new EditPage();
            navi.Navigate(edit);
        }

        public object EditPage_Xml(string ID)
        {
            var meta = new XmlDocument();
            meta.Load(@"./data/games/" + ID + "/meta.xml");
            return meta.SelectNodes("TOZAN");
        }
    }
}
