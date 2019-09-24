using System;
using System.Collections.Generic;
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

namespace TOZAN_Client
{
    /// <summary>
    /// 
    /// TOZAN Client
    /// 
    /// </summary>
    public partial class MainPage : Page
    {
        public MainPage()
        {
            //初期化
            InitializeComponent();
        }

        private void Start_Click(object sender, RoutedEventArgs e)
        {
            //ゲームのリストを表示
            ListPage list = new ListPage();
            NavigationService.Navigate(list);
        }
        private void Add_mountain_Click(object sender, RoutedEventArgs e)
        {
            //山データ追加ページに移動
            AddWindow addMountain = new AddWindow();
            addMountain.Show();
        }
        private void Settings_Click(object sender, RoutedEventArgs e)
        {
            //設定ページへ移動
            SettingsPage setpage = new SettingsPage();
            NavigationService.Navigate(setpage);
        }

        private void Mainpage_Loaded(object sender, RoutedEventArgs e)
        {
            //バージョン情報取得(仮)
            version.Text = "TOZAN Client Preview v" + System.Reflection.Assembly.GetExecutingAssembly().GetName().Version.ToString();
        }
    }
}
