using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Xml;

namespace TOZAN_Client
{
    /// <summary>
    /// ResourceChild_EditPage.xaml の相互作用ロジック
    /// </summary>
    public partial class ResourceChild_EditPage : UserControl
    {
        private string ID;
        private string Ex_Address;
        private string AD_Address;
        private string meta_Address;
        public ResourceChild_EditPage()
        {
            InitializeComponent();
            ID = (string)Application.Current.Properties["ID"];
            Ex_Address = "./data/games/" + ID + "/resources/s_view/";
            AD_Address = "./data/games/" + ID + "/resources/ads/";
            meta_Address = "./data/games/" + ID + "/meta.xml";
        }
        public ImageSource Source
        {
            get
            {
                return Ex_AD_Image.Source;
            }
            set
            {
                Ex_AD_Image.Source = value;
            }
        }
        public string FileName
        {
            get
            {
                return Image_FileName.Text;
            }
            set
            {
                Image_FileName.Text = value;
            }
        }
        private void ImagePreview_Click(object sender, RoutedEventArgs e)
        {

        }
        private void ImageDelete_Click(object sender, RoutedEventArgs e)
        {
            EditPage edit = new EditPage();
            XmlDocument meta = new XmlDocument();
            ImageSourceConverter converter = new ImageSourceConverter();
            string source = converter.ConvertToString(Source);
            string name = FileName;
            meta.Load(meta_Address);
            ResourceChild_EditPage Ex_Ad_Resource=new ResourceChild_EditPage();
            edit.EditListView.Children.Remove(Ex_Ad_Resource);
            XmlNodeList ex = meta.SelectNodes("TOZAN/resources/expansion/image");
            for(int i = 0; i < ex.Count; i++)
            {
                foreach(XmlNode image in ex)
                {
                    if (name == image.InnerText)
                    {
                        ex[i].ParentNode.RemoveChild(ex[i]);
                    }
                }
            }
            File.Delete(source);
            meta.Save(meta_Address);
            
            if (edit.ad_tab.IsChecked == true)
            {
                File.Delete(source);

                meta.Save(meta_Address);
            }
        }
    }
}
