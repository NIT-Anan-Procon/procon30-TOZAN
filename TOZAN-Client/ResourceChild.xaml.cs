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
    /// ResourceChild.xaml の相互作用ロジック
    /// </summary>
    public partial class ResourceChild : UserControl
    {
        public ResourceChild()
        {
            InitializeComponent();
        }

        public string Title
        {
            get
            {
                return ButtonTitle.Text;
            }
            set
            {
                ButtonTitle.Text = value;
            }
        }

        public string Location
        {
            get
            {
                return Address.Text;
            }
            set
            {
                Address.Text = value;
            }
        }

        public double Space
        {
            get
            {
                return TitleLocationSpace.Width;
            }
            set
            {
                TitleLocationSpace.Width = value;
            }
        }
        public string ID
        {
            get
            {
                return TOZANID.Text;
            }
            set
            {
                TOZANID.Text = value;
            }
        }

        public ImageSource Source
        {
            get
            {
                return ResourceImage.Source;
            }
            set
            {
                ResourceImage.Source = value;
            }
        }

        public bool SaveStatus
        {
            get
            {
                if (Save.Text=="")
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            set
            {
                if (value == true)
                {
                    Save.Text = "保存済み";
                }
                else
                {
                    Save.Text = "";
                }
            }
        }

        public double ProgressValue
        {
            get
            {
                return progress.Value;
            }
            set
            {
                progress.Value = value;
            }
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            Application.Current.Properties["ID"] = TOZANID.Text;
            GameWindow game = new GameWindow();
            game.Show();
        }

        private void MenuInfo_Click(object sender, RoutedEventArgs e)
        {
            PropertyWindow property = new PropertyWindow();
            property.Show();
        }

        private void MenuEdit_Click(object sender, RoutedEventArgs e)
        {
            Application.Current.Properties["ID"] = TOZANID.Text;
            Launch launch = new Launch();
            launch.EditPageMove(NavigationService.GetNavigationService(this));
        }
    }
}
