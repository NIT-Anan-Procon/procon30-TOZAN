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
using System.Diagnostics;
using System.Windows.Shapes;
using System.Xml;
using TOZAN_Client;
using System.Net;

namespace TOZAN_Client
{
    /// <summary>
    /// ListPage.xaml の相互作用ロジック
    /// </summary>
    public partial class ListPage : Page
    {
        public ListPage()
        {
            InitializeComponent();
        }

        List<string> errorCount = new List<string>();

        private string id;
        private string name;
        private string location;

        private void BackButton_Click(object sender, RoutedEventArgs e)
        {
            MainPage main = new MainPage();
            NavigationService.Navigate(main);
        }

        private void LoadPage_Loaded(object sender, RoutedEventArgs e)
        {
            if (!Directory.Exists("./data"))
            {
                Directory.CreateDirectory("./data/saves");
                Directory.CreateDirectory("./data/games");
            }
            LoadResourcesList();
        }

        private void LoadResourcesList()
        {
            IEnumerable<string> resources = Directory.EnumerateDirectories("./data/games", "*", SearchOption.TopDirectoryOnly); //全ての解凍された山データの取得
            foreach (string gameid in resources)
            {
                if (!File.Exists(gameid + "/meta.xml"))
                {
                    errorCount.Add(gameid);
                    continue;
                }
                var metaXml = new XmlDocument();
                metaXml.Load(gameid + "/meta.xml");
                try
                {
                    XmlNodeList idNode = metaXml.GetElementsByTagName("id");
                    XmlNodeList titleNode = metaXml.GetElementsByTagName("title");
                    XmlNodeList locationNode = metaXml.GetElementsByTagName("location");
                    id = idNode[0].InnerText;
                    name = titleNode[0].InnerText;
                    location = locationNode[0].InnerText;
                }
                catch (NullReferenceException)
                {
                    errorCount.Add(gameid);
                    continue;
                }

                ResourceChild resourceChild = new ResourceChild()                   //コントロールを定義
                {
                    Name = "t"+id                                                   //コントロール名にIDを設定
                };
                GamesListView.Children.Add(resourceChild);                          //リスト表示用のStackPanelに追加
                GamesListView.RegisterName(resourceChild.Name, resourceChild);      //リスト表示用のStackPanelに登録

                if (File.Exists("./data/games/" + id + "/preview.jpg")) {
                    var converter = new ImageSourceConverter();
                    resourceChild.Source = (ImageSource)converter.ConvertFromString("./data/games/" + id + "/preview.jpg");
                }
                resourceChild.Height = 300;
                resourceChild.Width = 400;
                resourceChild.Title = name;
                resourceChild.ID = id;
                resourceChild.Location = location;
                resourceChild.SaveStatus = true;

                Grid autoSpace = new Grid
                {
                    Name = "space"+id
                };
                GamesListView.Children.Add(autoSpace);                              //リスト表示用のStackPanelに追加
                GamesListView.RegisterName(autoSpace.Name, autoSpace);
                autoSpace.Width = 20;
            }
            if(errorCount.Count != 0)
            {
                FixResources();
            }
        }
        private void FixResources()
        {
            MessageBoxResult result = MessageBox.Show("破損した山データが見つかりました。再ダウンロードして修復しますか?", "情報", MessageBoxButton.YesNo, MessageBoxImage.Question, MessageBoxResult.Yes);
            if (result == MessageBoxResult.Yes)
            {

            }
            else
            {
                return;
            }
        }
    }
}
