using System;
using System.IO;
using System.Threading.Tasks;
using OneDevApp;
using UnityEngine;
using UnityEngine.UI;

public class NativePluginDemo : MonoBehaviour
{

    public Text infoText;

    public Button showToastBtn;
    public Button showAlertBtn;
    public Button showConfirmBtn;
    public Button showProgressBarBtn;
    public Button dismissProgressBarBtn;
    public Button showTimePickerBtn;
    public Button showDatePickerBtn;
    public Button RootStatusBtn;
    public Button openSettingsBtn;
    public Button checkPermissionBtn;
    public Button checkUpdateBtn;
    public Button startUpdateBtn;
    public Button toggleUpdateBtn;
    public Button toggleTypeBtn;
    public Button toggleLogBtn;
    public Button enableLocationBtn;
    public Button filePickerBtn;
    public Button shareContentBtn;
    public Button shareFileContentBtn;
    public Button shareMFilesContentBtn;
    public Button shareContentOnWPBtn;
    public Button shareContentOnMailBtn;
    public Image selectedImage;
    public GameObject imagePanel;

    private UpdateMode updateMode = UpdateMode.PLAY_STORE;
    private UpdateType updateType = UpdateType.FLEXIBLE;
    [SerializeField]
    private string m_thirdPartyLink = "";
    private bool toggleLog = false;
    private string[] permissions = { "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_CONTACTS", "android.permission.READ_CALENDAR" };
    private string[] filePaths = new string[2];
    private string selectedFileUri = string.Empty;

    private void OnEnable()
    {
        // Subscribe for events from NativeManager
        MobileNativeManager.OnUpdateAvailable += OnUpdateAvailable;
        MobileNativeManager.OnUpdateVersionCode += OnUpdateVersionCode;
        MobileNativeManager.OnUpdateStalenessDays += OnUpdateStalenessDays;
        MobileNativeManager.OnUpdateInstallState += OnUpdateInstallState;
        MobileNativeManager.OnUpdateDownloading += OnUpdateDownloading;
        MobileNativeManager.OnUpdateError += OnUpdateError;
        MobileNativeManager.OnPermissionGranted += OnPermissionGranted;
        MobileNativeManager.OnPermissionDenied += OnPermissionDenied;
        MobileNativeManager.OnPermissionError += OnPermissionError;
        MobileNativeManager.OnImagePicked += OnImagePicked;
        MobileNativeManager.OnClickAction += OnClickAction;
        MobileNativeManager.OnSelectedAction += OnSelectedAction;

        CacheScreenShot();
    }


    private void OnDisable()
    {
        // Unsubscribe for events from NativeManager
        MobileNativeManager.OnUpdateAvailable -= OnUpdateAvailable;
        MobileNativeManager.OnUpdateVersionCode -= OnUpdateVersionCode;
        MobileNativeManager.OnUpdateStalenessDays -= OnUpdateStalenessDays;
        MobileNativeManager.OnUpdateInstallState -= OnUpdateInstallState;
        MobileNativeManager.OnUpdateDownloading -= OnUpdateDownloading;
        MobileNativeManager.OnUpdateError -= OnUpdateError;
        MobileNativeManager.OnPermissionGranted -= OnPermissionGranted;
        MobileNativeManager.OnPermissionDenied -= OnPermissionDenied;
        MobileNativeManager.OnPermissionError -= OnPermissionError;
        MobileNativeManager.OnImagePicked -= OnImagePicked;
        MobileNativeManager.OnClickAction -= OnClickAction;
        MobileNativeManager.OnSelectedAction -= OnSelectedAction;
    }

    private void Start()
    {
        startUpdateBtn.interactable = false;
        dismissProgressBarBtn.interactable = false;

        showToastBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShowToast("Simple toast message from Unity", 0);
        });
        showAlertBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShowAlertMessage("Mobile Native Plugin", "Simple alert message from Unity");
        });
        showConfirmBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShowConfirmationMessage("Mobile Native Plugin", "Simple confirmation message from Unity");
        });
        showProgressBarBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShowProgressBar("Mobile Native Plugin", "Simple progressbar from Unity");
            dismissProgressBarBtn.interactable = true;
        });
        dismissProgressBarBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.DismissProgressBar();
            dismissProgressBarBtn.interactable = false;
        });
        showTimePickerBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShowTimePicker();
        });
        showDatePickerBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShowDatePicker();
        });
        RootStatusBtn.onClick.AddListener(() =>
        {
            Debug.Log("isDeviceRooted::" + MobileNativeManager.Instance.IsDeviceRooted());
        });
        openSettingsBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.OpenSettingScreen();
        });
        checkPermissionBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.RequestPermission(permissions);
        });

        checkUpdateBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.CheckForUpdate(updateMode, updateType, m_thirdPartyLink);
        });
        startUpdateBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.StartUpdate();

            startUpdateBtn.interactable = false;
            checkUpdateBtn.interactable = false;
        });
        toggleUpdateBtn.onClick.AddListener(() =>
        {
            if (updateMode == UpdateMode.PLAY_STORE)
                updateMode = UpdateMode.THIRD_PARTY;
            else
                updateMode = UpdateMode.PLAY_STORE;

            toggleUpdateBtn.GetComponentInChildren<Text>().text = "Update Mode : " + (updateMode == UpdateMode.PLAY_STORE ? " PLAY STORE" : " THIRD PARTY");
        });
        toggleTypeBtn.onClick.AddListener(() =>
        {
            if (updateType == UpdateType.FLEXIBLE)
                updateType = UpdateType.IMMEDIATE;
            else
                updateType = UpdateType.FLEXIBLE;

            toggleTypeBtn.GetComponentInChildren<Text>().text = "Update Type : " + (updateType == UpdateType.FLEXIBLE ? " Flexiable" : " IMMEDIATE");
        });
        toggleLogBtn.onClick.AddListener(() =>
        {
            toggleLog = !toggleLog;
            MobileNativeManager.Instance.PluginDebug(toggleLog);
            toggleLogBtn.GetComponentInChildren<Text>().text = "Log : " + (toggleLog ? " enabled" : " diabled");
        });

        toggleUpdateBtn.onClick.Invoke();
        toggleTypeBtn.onClick.Invoke();
        toggleLogBtn.onClick.Invoke();

        filePickerBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.GetImageFromDevice();
        });

        enableLocationBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.EnableLocation();
        });

        shareContentBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShareTextContent("This is test message to share (only Text)");
        });

        shareFileContentBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShareFileContent("This is test message to share with File content", string.IsNullOrEmpty(selectedFileUri) ? filePaths[0] : selectedFileUri, string.IsNullOrEmpty(selectedFileUri) ? false : true, "");
        });
        shareMFilesContentBtn.onClick.AddListener(() =>
        {
            MultipleFilesData filesData = new MultipleFilesData();
            filesData.filePath = filePaths;
            if (!string.IsNullOrEmpty(selectedFileUri))
                filesData.fileUri = new string[] { selectedFileUri };
            MobileNativeManager.Instance.ShareMultipleFileContent("This is test message to share multiple files", filesData, "");
        });
        shareContentOnWPBtn.onClick.AddListener(() =>
        {
            MobileNativeManager.Instance.ShareOnWhatsApp("This is test message to share on whatsapp", "", string.IsNullOrEmpty(selectedFileUri) ? filePaths[0] : selectedFileUri, string.IsNullOrEmpty(selectedFileUri) ? false : true, "");
        });
        shareContentOnMailBtn.onClick.AddListener(() =>
        {
            EmailSharingData emailData = new EmailSharingData();
            emailData.emailTo = new string[] { "onedevapp@gmail.com" };
            emailData.subject = "Reg: Test mail from Native Plugin";
            emailData.message = "This is test message to share on mail";
            emailData.isHtmlText = false;

            MultipleFilesData filesData = new MultipleFilesData();
            filesData.filePath = filePaths;
            if (!string.IsNullOrEmpty(selectedFileUri))
                filesData.fileUri = new string[] { selectedFileUri };

            emailData.fileData = filesData;
            MobileNativeManager.Instance.ShareOnMail(emailData, "");
        });
    }

    private async void CacheScreenShot()
    {
        await Task.Delay(500);
        //if(!File.Exists(getFilePath("FirstFile.png")))
        ScreenCapture.CaptureScreenshot("FirstFile.png");
        //if (!File.Exists(getFilePath("SecondFile.png")))

        await Task.Delay(500);
        ScreenCapture.CaptureScreenshot("SecondFile.png");

        filePaths[0] = getFilePath("FirstFile.png");
        filePaths[1] = getFilePath("SecondFile.png");

        Debug.Log("0::" + filePaths[0]);
        Debug.Log("1::" + filePaths[1]);
    }


    // Get path for given file
    private string getFilePath(string path)
    {
#if UNITY_EDITOR
        return Application.dataPath + Path.DirectorySeparatorChar + path;
#elif UNITY_ANDROID
            return Application.persistentDataPath + Path.DirectorySeparatorChar + path;
#endif
    }

    private void OnUpdateError(int code, string error)
    {
        if (code == (int)InstallErrorCode.ERROR_LIBRARY)
            Debug.Log("Error : " + error);
        else
            Debug.Log("Error Code : " + (InstallErrorCode)code + " :: " + error);
        startUpdateBtn.interactable = false;
        checkUpdateBtn.interactable = true;

    }

    private void OnUpdateDownloading(long bytesDownloaded, long bytesTotal)
    {
        int downloadProgress = (int)((bytesDownloaded * 100) / bytesTotal);
        infoText.text = "Downloading : " + bytesDownloaded.ToPrettySize(false, 1) + "/" + bytesTotal.ToPrettySize(true, 1) + " ( " + downloadProgress + "% )";
    }

    private void OnUpdateInstallState(InstallStatus state)
    {
        infoText.text = "Install State : " + state.ToString();

        if (state == InstallStatus.DOWNLOADED)
        {
            MobileNativeManager.Instance.CompleteUpdate();
            startUpdateBtn.interactable = false;
            checkUpdateBtn.interactable = true;
        }
    }

    private void OnUpdateStalenessDays(int days)
    {
        Debug.Log("Staleness Days : " + days);
    }

    private void OnUpdateVersionCode(int versionCode)
    {
        Debug.Log("Version code : " + versionCode);
    }

    private void OnUpdateAvailable(bool isUpdateAvailable)
    {
        infoText.text = "Is update Available : " + isUpdateAvailable;

        startUpdateBtn.interactable = isUpdateAvailable;
    }

    private void OnPermissionDenied(string[] deniedPermissions)
    {
        Debug.Log("OnPermissionDenied : We got some denied permissions.");
    }

    private void OnPermissionGranted(string[] grantedPermissions, bool all)
    {
        if (all)
        {
            Debug.Log("OnPermissionGranted : All permissions are granted. You good to go.");
            if (grantedPermissions[0] == "android.permission.ACCESS_FINE_LOCATION")
            {
                MobileNativeManager.Instance.EnableLocation();
            }
        }
        else
        {
            Debug.Log("OnPermissionGranted : Some permissions are denied.");
        }
    }

    private void OnPermissionError(string error)
    {
        Debug.Log("Error : " + error);
    }

    private void OnImagePicked(ImageData imageData, string errorMessage, ImagePickerErrorCode errorCode)
    {
        if (errorCode == ImagePickerErrorCode.NONE)
        {
            Debug.Log("OnImagePicked::success");

            try
            {

                string extension = Path.GetExtension(imageData.cacheFilePath).ToLowerInvariant();
                TextureFormat format = (extension == ".jpg" || extension == ".jpeg") ? TextureFormat.RGB24 : TextureFormat.RGBA32;

                int w = imageData.width;
                int h = imageData.height;

                Texture2D result = new Texture2D(w, h, format, true, false);

                if (result.LoadImage(File.ReadAllBytes(imageData.cacheFilePath), true))
                {
                    Sprite newSprite = Sprite.Create(result as Texture2D, new Rect(0f, 0f, result.width, result.height), Vector2.zero);
                    selectedImage.sprite = newSprite;
                    imagePanel.SetActive(true);

                    selectedFileUri = imageData.uri;
                }
            }
            catch (Exception e)
            {
                Debug.LogException(e);

            }
            finally
            {

                try
                {
                    File.Delete(imageData.cacheFilePath);
                }
                catch { }
            }
        }
        else
        {
            Debug.Log("OnImagePicked::failed::" + errorMessage);
        }
    }

    private void OnClickAction(bool action)
    {
        Debug.Log("OnClickAction clicked:: " + action);
    }

    private void OnSelectedAction(string value)
    {
        Debug.Log("OnSelectedAction selected:: " + value);
    }
}
