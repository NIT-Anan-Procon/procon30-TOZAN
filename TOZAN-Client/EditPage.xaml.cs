using Microsoft.Maps.MapControl.WPF;
using Microsoft.Win32;
using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Threading;
using System.Xml;
using Path = System.IO.Path;

namespace TOZAN_Client
{
    /// <summary>
    /// EditPage.xaml の相互作用ロジック
    /// </summary>
    public partial class EditPage : Page
    {
        private int count;
        private string ID;
        private string Ex_Address;
        private string AD_Address;
        private string meta_Address;

        public EditPage()
        {
            InitializeComponent();
            ID = (string)Application.Current.Properties["ID"];
            Ex_Address = "./data/games/" + ID + "/resources/s_view/";
            AD_Address = "./data/games/" + ID + "/resources/ads/";
            meta_Address = "./data/games/" + ID + "/meta.xml";
        }
        public bool menuState = false;
        private void AddButton_Checked(object sender, RoutedEventArgs e)
        {
            
        }

        private void AddButton_Unchecked(object sender, RoutedEventArgs e)
        {

        }

        private void Ad_tab_Checked(object sender, RoutedEventArgs e)
        {
            zekkei_text.Foreground = Brushes.DimGray;
            ad_text.Foreground = Brushes.White;

            //meta.xml参照
            XmlDocument meta = new XmlDocument();
            meta.Load(meta_Address);

            ResourceChild_EditPage Ex_Resource = new ResourceChild_EditPage();
            //絶景で表示されていた写真の削除
            for (int i = 0; i < EditListView.Children.Count; i++)
            {
                EditListView.Children.Remove(Ex_Resource);
            }
            meta.Load(meta_Address);
            XmlNodeList ad_image = meta.SelectNodes("TOZAN/resources/expansion/ads");
            foreach(XmlNode image in ad_image)
            {
                ResourceChild_EditPage Ad_Resource = new ResourceChild_EditPage();
            }
        }

        private void Zekkei_tab_Checked(object sender, RoutedEventArgs e)
        {
            ad_text.Foreground = Brushes.DimGray;
            zekkei_text.Foreground = Brushes.White;

            //meta.xml参照
            XmlDocument meta = new XmlDocument();
            meta.Load(meta_Address);

            ResourceChild_EditPage Ad_Resource = new ResourceChild_EditPage();
            //広告で表示されていた写真の削除
            for (int i = 0; i < EditListView.Children.Count; i++)
            {
                EditListView.Children.Remove(Ad_Resource);
            }
            meta.Load(meta_Address);
            XmlNode ex_image = meta.SelectSingleNode("TOZAN/resources/expansion/image");

        }

        private void EditPage_Loaded(object sender, RoutedEventArgs e)
        {
            zekkei_tab.IsChecked = true;
            InfoButton.IsChecked = true;

            //meta.xml参照
            XmlDocument meta = new XmlDocument();
            meta.Load(meta_Address);

            XmlNode h = meta.SelectSingleNode("TOZAN/height");
            XmlNode r = meta.SelectSingleNode("TOZAN/route");
            XmlNode d = meta.SelectSingleNode("TOZAN/description");

            Console.WriteLine("\nh:"+h.InnerText+"\nr:"+r.InnerText + "\nd:" + d.InnerText+"\n");

            EditPageMap.Mode = new AerialMode();
            id.Text = ID;
            mount.Text = meta.SelectSingleNode("TOZAN/title").InnerText;

            if (h.InnerText == "")
            {
                height.Visibility = Visibility.Collapsed;
            }
            else
            {
                height_text.Visibility = Visibility.Collapsed;
                height.Visibility = Visibility.Visible;
                height.Text = h.InnerText;
            }
            if (r.InnerText == "")
            {
                route.Visibility = Visibility.Collapsed;
            }
            else
            {
                route_text.Visibility = Visibility.Collapsed;
                route.Visibility = Visibility.Visible;
                route.Text = r.InnerText;
            }
            if (d.InnerText == "")
            {
                description.Visibility = Visibility.Collapsed;
            }
            else
            {
                description_text.Visibility = Visibility.Collapsed;
                description.Visibility = Visibility.Visible;
                description.Text = d.InnerText;
            }
            if (h.InnerText == "" || r.InnerText == "" || d.InnerText == "")
            {
                MessageBoxResult attension = MessageBox.Show("山データに足りていない情報を追加してください。", "情報", MessageBoxButton.OK);
                if (attension == MessageBoxResult.OK) { }
            }
            XmlNodeList img = meta.SelectNodes("TOZAN/resources/normal/image");
            foreach(XmlNode image in img)
            {
                double latitude;
                double longitude;
                int center = (img.Count)/2;
                if (image.Attributes["latitude"].InnerText != "none" && image.Attributes["longitude"].InnerText != "none")
                {
                    latitude = double.Parse(image.Attributes["latitude"].InnerText);
                    longitude = double.Parse(image.Attributes["longitude"].InnerText);
                    if (center == count)
                    {
                        EditPageMap.Center = new Location(latitude, longitude);
                    }
                    var pushpin = new Pushpin();
                    MapLayer.SetPosition(pushpin, new Location(latitude, longitude));
                    EditPageMap.ZoomLevel = 16;
                    EditPageMap.Children.Add(pushpin);
                    count++;
                }
            }
        }

        private void PreviewButton_Checked(object sender, RoutedEventArgs e)
        {
            
        }

        private void BackButton_Click(object sender, RoutedEventArgs e)
        {
            ListPage list = new ListPage();
            NavigationService.Navigate(list);
        }

        private void Add_Picture_Click(object sender, RoutedEventArgs e)
        {
            // ダイアログのインスタンスを生成
            var dialog = new OpenFileDialog();
            dialog.Title = "追加する写真の選択";
            //標準で開くフォルダの指定
            dialog.InitialDirectory = System.Environment.GetFolderPath(Environment.SpecialFolder.CommonPictures);
            // ファイルの種類を設定
            dialog.Filter = "写真ファイル (*.jpg)|*.jpg";
            // ダイアログを表示する
            if (dialog.ShowDialog() == true)
            {
                XmlDocument meta = new XmlDocument();
                meta.Load(meta_Address);
                //開いたファイルをゲームフォルダにコピーしてxmlファイルに書き込み
                if (zekkei_tab.IsChecked==true)
                {
                    File.Copy(dialog.FileName, Ex_Address+Path.GetFileName(dialog.FileName), true);
                    XmlNode ex_image = meta.CreateElement("image");
                    ex_image.InnerText = Path.GetFileName(dialog.FileName);
                    XmlNode ex = meta.SelectSingleNode("TOZAN/resources/expansion");
                    ex.AppendChild(ex_image);
                    meta.Save(meta_Address);
                }
                if (ad_tab.IsChecked == true)
                {
                    File.Copy(dialog.FileName, AD_Address + Path.GetFileName(dialog.FileName), true);
                    XmlNode ad_image = meta.CreateElement("image");
                    ad_image.InnerText = Path.GetFileName(dialog.FileName);
                    XmlNode ad = meta.SelectSingleNode("TOZAN/resources/ads");
                    ad.AppendChild(ad_image);
                    meta.Save(meta_Address);
                }
            }
        }

        private void Info_edit_Click(object sender, RoutedEventArgs e)
        {
            //xml読み込み
            XmlDocument meta = new XmlDocument();
            meta.Load(meta_Address);
            //要素の選択
            XmlNode h = meta.SelectSingleNode("TOZAN/height");
            XmlNode r = meta.SelectSingleNode("TOZAN/route");
            XmlNode d = meta.SelectSingleNode("TOZAN/description");
            //TextBoxのを見えなくするやつ
            height.Visibility = Visibility.Collapsed;
            height_text.Visibility = Visibility.Visible;
            height_text.Text = h.InnerText;
            route.Visibility = Visibility.Collapsed;
            route_text.Visibility = Visibility.Visible;
            route_text.Text = r.InnerText;
            description.Visibility = Visibility.Collapsed;
            description_text.Visibility = Visibility.Visible;
            description_text.Text = d.InnerText;
        }

        private void Preview_pic_add_Click(object sender, RoutedEventArgs e)
        {
            // ダイアログのインスタンスを生成
            var dialog = new OpenFileDialog();
            dialog.Title = "追加する写真の選択";
            //標準で開くフォルダの指定
            dialog.InitialDirectory = System.Environment.GetFolderPath(Environment.SpecialFolder.CommonPictures);
            // ファイルの種類を設定
            dialog.Filter = "写真ファイル (*.jpg)|*.jpg";
            // ダイアログを表示する
            if (dialog.ShowDialog() == true)
            {
                File.Copy(dialog.FileName, "./data/games/" + ID + "/preview.jpg", true);
            }
        }

        private void Info_update_Click(object sender, RoutedEventArgs e)
        {
            //meta.xml参照
            XmlDocument meta = new XmlDocument();
            meta.Load(meta_Address);
            XmlNode h = meta.SelectSingleNode("TOZAN/height");
            XmlNode r = meta.SelectSingleNode("TOZAN/route");
            XmlNode d = meta.SelectSingleNode("TOZAN/description");
            h.InnerText = height_text.Text;
            r.InnerText = route_text.Text;
            d.InnerText = description_text.Text;
            meta.Save(meta_Address);
            //ページのリロード
            NavigationService.Refresh();
        }


        private void InfoButton_Click(object sender, RoutedEventArgs e)
        {
            //meta.xml参照
            XmlDocument meta = new XmlDocument();
            meta.Load(meta_Address);
            //山の説明のどれかが抜けていたら表示するウィンドウ
            XmlNode h = meta.SelectSingleNode("TOZAN/height");
            XmlNode r = meta.SelectSingleNode("TOZAN/route");
            XmlNode d = meta.SelectSingleNode("TOZAN/description");
            if (h.InnerText == "" || r.InnerText == "" || d.InnerText == "")
            {
                MessageBoxResult attension = MessageBox.Show("山データに足りていない情報を追加してください。", "情報", MessageBoxButton.OK);
                if (attension == MessageBoxResult.OK) { }
            }
        }
    }
}
