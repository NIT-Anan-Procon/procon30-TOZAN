using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using System.Windows.Threading;
using System.Xml;

namespace TOZAN_Client
{
    /// <summary>
    /// GameWindow.xaml の相互作用ロジック
    /// </summary>
    public partial class GameWindow : Window
    {
        public GameWindow()
        {
            InitializeComponent();
        }

        List<ImageSource> CurrentGame = new List<ImageSource>();
        int CurrentImage = 0;
        public void KeyOperations(object sender, KeyEventArgs e)
        {
            //ESCキーが押されたことを取得, メニューの状態を参照して開閉
            if (e.Key == Key.Escape)
            {
                if (MenuToggleEventTrigger.IsChecked == false)
                {
                    //メニューを開く
                    PauseMenuOverlay.IsEnabled = true;
                    PauseMenuOverlay.BeginAnimation(OpacityProperty, new DoubleAnimation(1, TimeSpan.FromSeconds(0.2)));
                    MenuToggleEventTrigger.IsChecked = true;
                }
                else
                {
                    //メニューを閉じる
                    PauseMenuOverlay.IsEnabled = false;
                    PauseMenuOverlay.BeginAnimation(OpacityProperty, new DoubleAnimation(0, TimeSpan.FromSeconds(0.2)));
                    MenuToggleEventTrigger.IsChecked = false;
                }
            }
            else if (e.Key == Key.Space)
            {
                CurrentImage++;
                Front.Source = CurrentGame[CurrentImage];
            }
        }

        private async void Window_Loaded(object sender, RoutedEventArgs e)
        {
            //各レイヤーの初期化
            PauseMenuOverlay.Opacity = 0;
            LoadingFrame.Opacity = 0;
            PauseMenuOverlay.Visibility = Visibility.Visible;
            FullScreenButton.IsChecked = true;

            await Task.Delay(500);
            LoadingFrame.BeginAnimation(OpacityProperty, new DoubleAnimation(1, TimeSpan.FromSeconds(1)));
            await Task.Delay(1000);

            GetAllResources(Application.Current.Properties["ID"].ToString());

            Front.Source = CurrentGame[CurrentImage];
        }

        private ImageSource ImageStringConverter(string source)
        {
            var converter = new ImageSourceConverter();
            return (ImageSource)converter.ConvertFromString(source);
        }

        private void GetAllResources(string id)
        {
            var metaXml = new XmlDocument();
            metaXml.Load("./data/games/" + id + "/meta.xml");
            XmlNodeList imageNode = metaXml.SelectNodes("TOZAN/resources/normal/image");
            foreach (XmlNode imageName in imageNode)
            {
                CurrentGame.Add(ImageStringConverter("./data/games/"+id+"/resources/n_view/"+imageName.InnerText));
            }

        }

        /*デバッグ用仮コード
        void timer_Tick(object sender, EventArgs e)
        {
            if (downloadProgress.Value == 100)
            {
                timer.Stop();
                LoadingFrame.BeginAnimation(OpacityProperty, new DoubleAnimation(0, TimeSpan.FromSeconds(1)));
                //LoadGame game = new LoadGame();
                //game.LoadResources(-1);

            }
            else
            {
                downloadProgress.Value++;
                percent.Text = downloadProgress.Value + "%";
            }
        }
        */

        private void FullScreenButton_Checked(object sender, RoutedEventArgs e)
        {
            //全画面表示
            WindowStyle = WindowStyle.None;
            WindowState = WindowState.Maximized;
        }

        private void FullScreenButton_Unchecked(object sender, RoutedEventArgs e)
        {
            //ウィンドウ表示
            WindowStyle = WindowStyle.SingleBorderWindow;
            WindowState = WindowState.Normal;
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            //ゲームウィンドウを閉じるかどうかのダイアログボックス表示
            MessageBoxResult result = MessageBox.Show("ゲームを終了しますか?", "情報", MessageBoxButton.YesNo, MessageBoxImage.Question, MessageBoxResult.Cancel);
            if (result == MessageBoxResult.No)
            {
                e.Cancel = true;
            }
        }

        private void CloseButton_Click(object sender, RoutedEventArgs e)
        {
            Close();
        }
    }
}
