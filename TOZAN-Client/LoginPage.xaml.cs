using Ionic.Zip;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
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
    /// LoginPage.xaml の相互作用ロジック
    /// </summary>
    public partial class LoginPage : Page
    {
        public LoginPage()
        {
            InitializeComponent();
        }
        private void BackButton_Click(object sender, RoutedEventArgs e)
        {
            MainPage main = new MainPage();
            NavigationService.Navigate(main);
        }

        private void Login_button_Click(object sender, RoutedEventArgs e)
        {
            /*EditPage edit = new EditPage();
            string ID = TOZAN_ID.Text;
            Application.Current.Properties["ID"] = TOZAN_ID.Text;
            string password = Password.Password;

            //TOZAN-IDが入力されなかった場合の処理
            if (ID != "" && password != "")
            {
                if (System.IO.File.Exists(@".\data\temp\" + ID + ".zip"))
                {
                    //Zip解凍
                    string input = @".\data\temp\" + ID + ".zip";
                    string output = @".\data\games\" + ID;
                    using (var zip = new Ionic.Zip.ZipFile(input, Encoding.GetEncoding("utf-8")))
                    NavigationService.Navigate(edit);
                }
                else
                {
                    var result = MessageBox.Show("山データがありません。ダウンロードしますか？", "情報", MessageBoxButton.YesNo);
                    if (result == MessageBoxResult.Yes)
                    {
                    }
                    else { }
                }
            }
            else
            {
                var result = MessageBox.Show("TOZAN-IDまたはパスワードを入力してください。", "情報", MessageBoxButton.OK);
                if (result == MessageBoxResult.OK) { }
            }*/
        }

        private void TOZAN_ID_PreviewTextInput(object sender, TextCompositionEventArgs e)
        {
            //数字のみの入力制限
            int result = 0;
            if (int.TryParse(e.Text, out result))
            { }
            else
            {
                e.Handled = true;
            }
        }
        private void TOZAN_ID_PreviewExecuted(object sender, ExecutedRoutedEventArgs e)
        {
            //TOZAN-IDのペースト制限
            if (e.Command == ApplicationCommands.Copy ||
                e.Command == ApplicationCommands.Cut ||
                e.Command == ApplicationCommands.Paste)
            {
                e.Handled = true;
            }
        }
    }
}
