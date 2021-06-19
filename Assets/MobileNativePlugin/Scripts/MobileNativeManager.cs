using System;
using UnityEngine;

namespace OneDevApp
{
    /// <summary>
    /// MobileNativeManager is a single intance class which calls Native Android APIs
    /// </summary>
    public class MobileNativeManager : MonoBehaviour
    {
#region Events
        
#pragma warning disable 0067
        /// <summary>
        /// Event triggered when the update is available or not
        /// </summary>
        public static event Action<bool> OnUpdateAvailable;
        /// <summary>
        /// Event triggered with version code available
        /// </summary>
        public static event Action<int> OnUpdateVersionCode;
        /// <summary>
        /// Event triggered with staleness days available to download
        /// </summary>
        public static event Action<int> OnUpdateStalenessDays;
        /// <summary>
        /// Event triggered with install state during update
        /// </summary>
        public static event Action<InstallStatus> OnUpdateInstallState;
        /// <summary>
        /// Event triggered with downloading value
        /// </summary>
        public static event Action<long, long> OnUpdateDownloading;
        /// <summary>
        /// Event triggered with error during update
        /// </summary>
        public static event Action<int, string> OnUpdateError;
        /// <summary>
        /// Event triggered with permissions granted
        /// </summary>
        public static event Action<string[], bool> OnPermissionGranted;
        /// <summary>
        /// Event triggered with permissions denied
        /// </summary>
        public static event Action<string[]> OnPermissionDenied;
        /// <summary>
        /// Event triggered with error when during permissions
        /// </summary>
        public static event Action<string> OnPermissionError;
        /// <summary>
        /// Event triggered with image picked
        /// </summary>
        public static event Action<ImageData, string, ImagePickerErrorCode> OnImagePicked;
        /// <summary>
        /// Event triggered when dialog buttons are clicked
        /// </summary>
        public static event Action<bool> OnClickAction;
        /// <summary>
        /// Event triggered with value when selected
        /// </summary>
        public static event Action<string> OnSelectedAction;

 #pragma warning restore 0067
#endregion

        public static MobileNativeManager Instance { get; private set; }

#pragma warning disable 0414
        /// <summary>
        /// UnityMainActivity current activity name or main activity name
        /// Modify only if this UnityPlayer.java class is extends or used any other default class
        /// </summary>
        [Tooltip("Android Launcher Activity")]
        [SerializeField]
        private string m_unityMainActivity = "com.unity3d.player.UnityPlayer";

        const string m_bridgePackageName = "com.onedevapp.nativeplugin.AndroidBridge";
        bool writeLog = false;

#pragma warning restore 0414

#if UNITY_ANDROID && !UNITY_EDITOR
        private AndroidJavaObject mContext = null;
        private AndroidJavaObject mUpdateManager = null;

        class OnImageSelectedListener : AndroidJavaProxy
        {
            public OnImageSelectedListener() : base("com.onedevapp.nativeplugin.imagepicker.OnImageSelectedListener") { }

            public void onImageSelected(bool status, string message, int errorCode)
            {
                if(OnImagePicked != null)
                {	
                
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {

                        ImageData imageData = null;
                        if (status)
                        {
                            Debug.Log("OnImagePicked::message::" + message);
                            imageData = JsonUtility.FromJson<ImageData>(message);
                            message = string.Empty;
                        }

                        OnImagePicked.Invoke(imageData, message, (ImagePickerErrorCode) errorCode);
                    });
                }
            }
        }

        class OnUpdateListener : AndroidJavaProxy
        {
            public OnUpdateListener() : base("com.onedevapp.nativeplugin.inappupdate.OnUpdateListener") { }

            // Invoked when Google Play Services returns a response 
            public void onUpdateAvailable(bool isUpdateAvailable, bool isUpdateTypeAllowed)
            {
                if (isUpdateAvailable && isUpdateTypeAllowed)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {

                        if (OnUpdateAvailable != null)
                        {
                            OnUpdateAvailable.Invoke(true);
                        }
                    });
                }
                else
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {

                        if (OnUpdateAvailable != null)
                        {
                            OnUpdateAvailable.Invoke(false);
                        }
                    });
                }
            }

            // Invoked when the update is available with version code
            public void onUpdateVersionCode(int versionCode)
            {
                if (OnUpdateVersionCode != null)
                {

                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnUpdateVersionCode.Invoke(versionCode);
                    });
                }
            }

            // Invoked when the update is available with staleness days
            public void onUpdateStalenessDays(int days)
            {
                if (OnUpdateStalenessDays != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnUpdateStalenessDays.Invoke(days);
                    });
                }
            }

            // Invoked when install status of the update
            public void onUpdateInstallState(int state)
            {
                if (OnUpdateInstallState != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnUpdateInstallState.Invoke((InstallStatus)state);
                    });
                }
            }

            // Invoked during downloading
            public void onUpdateDownloading(long bytesDownloaded, long bytesTotal)
            {
                if (OnUpdateDownloading != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnUpdateDownloading.Invoke(bytesDownloaded, bytesTotal);
                    });
                }
            }

            // Invoked when the update encounter error
            public void onUpdateError(int code, string error)
            {
                if (OnUpdateError != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnUpdateError.Invoke(code, error);
                    });
                }
            }
        }

        class OnPermissionListener : AndroidJavaProxy
        {
            public OnPermissionListener() : base("com.onedevapp.nativeplugin.rt_permissions.OnPermissionListener") { }


            public void onPermissionGranted(string[] grantPermissions, bool all)
            {
                if (OnPermissionGranted != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnPermissionGranted.Invoke(grantPermissions, all);
                    });
                }
            }

            public void onPermissionDenied(string[] deniedPermissions)
            {
                if (OnPermissionDenied != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnPermissionDenied.Invoke(deniedPermissions);
                    });
                }
            }

            public void onPermissionError(string errorMessage)
            {
                if (OnPermissionError != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnPermissionError.Invoke(errorMessage);
                    });
                }
            }
        }

        class OnClickPositiveListener : AndroidJavaProxy
        {
            public OnClickPositiveListener() : base("com.onedevapp.nativeplugin.AndroidBridge$OnClickListener") { }

            public void onClick()
            {
                if (OnClickAction != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnClickAction.Invoke(true);
                    });
                }
            }
        }

        class OnClickNegativeListener : AndroidJavaProxy
        {
            public OnClickNegativeListener() : base("com.onedevapp.nativeplugin.AndroidBridge$OnClickListener") { }

            public void onClick()
            {
                if (OnClickAction != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnClickAction.Invoke(false);
                    });
                }
            }
        }

        class OnSelectedListener : AndroidJavaProxy
        {
            public OnSelectedListener() : base("com.onedevapp.nativeplugin.AndroidBridge$OnSelectListener") { }

            public void onSelected(string selectedValue)
            {
                if (OnSelectedAction != null)
                {
                    UnityMainThreadDispatcher.Instance().Enqueue(() => {
                        OnSelectedAction.Invoke(selectedValue);
                    });
                }
            }
        }
#endif

        private void Awake()
        {
            if (Instance == null)
            {
                Instance = this;
            }
            else
            {
                DestroyImmediate(Instance.gameObject);
                Instance = this;
            }

#if UNITY_ANDROID && !UNITY_EDITOR
            if (Application.platform == RuntimePlatform.Android)
            {
                mContext = new AndroidJavaClass(m_unityMainActivity).GetStatic<AndroidJavaObject>("currentActivity");
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }


        #region App Update
        /// <summary>
        /// Check for update and returns OnUpdateListener.onUpdateAvailable true or false
        /// </summary>
        /// <param name="updateMode">update mode</param>
        /// <param name="updatetype">update type</param>
        /// <param name="apkLink">new apk link</param>
        public void CheckForUpdate(UpdateMode updateMode = UpdateMode.PLAY_STORE, UpdateType updateType = UpdateType.FLEXIBLE, string thirdPartyLink = "")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Update Manager

            var manager = new AndroidJavaClass("com.onedevapp.nativeplugin.inappupdate.UpdateManager");

            var mUpdateManager = manager.CallStatic<AndroidJavaObject>("Builder", mContext);

            mUpdateManager.Call<AndroidJavaObject>("updateMode", (int)updateMode)
                .Call<AndroidJavaObject>("handler", new OnUpdateListener())
                .Call<AndroidJavaObject>("updateType", (int)updateType);

            if (!string.IsNullOrEmpty(thirdPartyLink))
                mUpdateManager.Call<AndroidJavaObject>("updateLink", thirdPartyLink);

            mUpdateManager.Call("checkUpdate");
#elif UNITY_EDITOR
            if(writeLog)                
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// To start the instalation of the update, you should call this function after OnUpdateListener.onUpdate(isUpdateAvailable, isUpdateTypeAllowed)
        /// and only if both isUpdateAvailable and isUpdateTypeAllowed are true 
        /// </summary>
        public void StartUpdate()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            if (mUpdateManager != null)
                mUpdateManager.Call("startUpdate");
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// To complete the instalation of the update, you should call this function after OnUpdateListener.onUpdateInstallState().
        /// This has no impact while using third party link update
        /// </summary>
        public void CompleteUpdate()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            if (mUpdateManager != null)
                mUpdateManager.Call("completeUpdate");
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// To continue update and must can called only on app onresume to complete the pending update of previous update
        /// </summary>
        public void ContinueUpdate()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            if (mUpdateManager != null)
                mUpdateManager.Call("continueUpdate");
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        #endregion

        #region App Permission
        /// <summary>
        /// Check for permission and returns status in OnPermissionListener.onPermissionGranted()
        /// </summary>
        /// <param name="permissions">Array of requested permissions</param>
        public void RequestPermission(params string[] permissions)
        {

#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Permission Manager
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.rt_permissions.PermissionManager"))
            {
                var mPermissionManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);
                mPermissionManager
                    .Call<AndroidJavaObject>("handler", new OnPermissionListener())
                    .Call<AndroidJavaObject>("addPermissions", javaArrayFromCS(permissions))
                    .Call("requestPermission");
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Check for permission and returns status in OnPermissionListener.onPermissionGranted()
        /// </summary>
        /// <param name="permissions">Array of requested permissions</param>
        public void RequestPermission(string permission)
        {

#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Permission Manager
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.rt_permissions.PermissionManager"))
            {
                var mPermissionManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);
                mPermissionManager
                    .Call<AndroidJavaObject>("handler", new OnPermissionListener())
                    .Call<AndroidJavaObject>("addPermission", permission)
                    .Call("requestPermission");
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Check whether permission is granted or not
        /// </summary>
        /// <param name="permissions">Array of requested permissions</param>
        /// <returns>Returns True if already permission granted for this permission else false.</returns>
        public bool CheckPermission(string permission)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                return jc.CallStatic<bool>("CheckPermission", mContext, permission);
            }
#elif UNITY_EDITOR
            return true;
#endif
        }

        /// <summary>
        /// Check whether permission permission can show rationale dialog
        /// </summary>
        /// <returns>Returns True if permission is requested but not granted and can show rationale dialog else false.</returns>
        public bool CheckPermissionRationale(string permission)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                return jc.CallStatic<bool>("CheckPermissionRationale", mContext, permission);
            }
#elif UNITY_EDITOR
            return true;
#endif
        }
        #endregion

        #region ImagePicker

        /// <summary>
        /// Get image from device via Camera or Gallery
        /// </summary>
        /// <param name="pickerType">ImagePickerType of Choice or Camera or Gallery</param>
        /// <param name="maxWidth">image max width to compress</param>
        /// <param name="maxHeight">image max height to compress</param>
        /// <param name="quality">image quality from 1 to 100</param>
        public void GetImageFromDevice(ImagePickerType pickerType = ImagePickerType.CHOICE, int maxWidth = 612, int maxHeight = 816, int quality = 80)
        {

#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize ImagePicker Manager

            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.imagepicker.ImagePickerManager"))
            {
                var mImagePickerManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);
                mImagePickerManager
                    .Call<AndroidJavaObject>("setPickerType", (int)pickerType)
                    .Call<AndroidJavaObject>("setMaxWidth", maxWidth)
                    .Call<AndroidJavaObject>("handler", new OnImageSelectedListener())
                    .Call<AndroidJavaObject>("setMaxHeight", maxHeight)
                    .Call<AndroidJavaObject>("setQuality", quality)
                    .Call("openImagePicker");
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        #endregion

        #region Share
        /// <summary>
        /// Share on whatsapp
        /// </summary>
        /// <param name="message">message to be shared</param>
        /// <param name="mobileNo">to share on specific phone no</param>
        /// <param name="filePath">file path/ file Uri to be shared</param>
        /// <param name="isFileUri">bool which represent filepath is path or uri, if true then filePath takes as Uri</param>
        /// <param name="header">share chooser header text</param>
        public void ShareOnWhatsApp(string message, string mobileNo = "", string filePath = "", bool isFileUri = false, string header = "")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Share Manager
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.share.ShareManager"))
            {
                var mShareManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);

                mShareManager.Call<AndroidJavaObject>("setMessage", message);

                if (!string.IsNullOrEmpty(filePath))
                    mShareManager.Call<AndroidJavaObject>(isFileUri ? "addFileUri" : "addFilePath", filePath);
                if (!string.IsNullOrEmpty(mobileNo))
                    mShareManager.Call<AndroidJavaObject>("setWhatsAppMobileNo", mobileNo);
                if (!string.IsNullOrEmpty(header))
                    mShareManager.Call<AndroidJavaObject>("setHeader", header);

                mShareManager.Call("shareOnWhatsApp");
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }
        /// <summary>
        /// Share Text
        /// </summary>
        /// <param name="message">message to be shared</param>
        /// <param name="header">share chooser header text</param>
        public void ShareTextContent(string message, string header = "")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Share Manager
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.share.ShareManager"))
            {
                var mShareManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);

                mShareManager.Call<AndroidJavaObject>("setMessage", message);

                if (!string.IsNullOrEmpty(header))
                    mShareManager.Call<AndroidJavaObject>("setHeader", header);

                mShareManager.Call("shareTextContent");
            }

#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Share Single File
        /// </summary>
        /// <param name="message">message to be shared</param>
        /// <param name="filePath">file path/ file Uri to be shared</param>
        /// <param name="isFileUri">bool which represent filepath is path or uri, if true then filePath takes as Uri</param>
        /// <param name="header">share chooser header text</param>
        public void ShareFileContent(string message, string filePath = "", bool isFileUri = false, string header = "")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Share Manager
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.share.ShareManager"))
            {
                var mShareManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);

                mShareManager.Call<AndroidJavaObject>("setMessage", message);

                if (!string.IsNullOrEmpty(filePath))
                    mShareManager.Call<AndroidJavaObject>(isFileUri ? "addFileUri" : "addFilePath", filePath);
                if (!string.IsNullOrEmpty(header))
                    mShareManager.Call<AndroidJavaObject>("setHeader", header);

                mShareManager.Call("shareFileContent");
            }

#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Share Multiple Files
        /// </summary>
        /// <param name="message">message to be shared</param>
        /// <param name="fileData">MultipleFilesData which holds array of file path and file Uri</param>
        /// <param name="header">share chooser header text</param>
        public void ShareMultipleFileContent(string message, MultipleFilesData fileData, string header = "")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Share Manager
            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.share.ShareManager"))
            {
                var mShareManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);

                mShareManager.Call<AndroidJavaObject>("setMessage", message);

                if (fileData.filePath != null || fileData.filePath.Length > 0)
                {
                    mShareManager.Call<AndroidJavaObject>("addMultipleFilePaths", javaArrayFromCS(fileData.filePath));
                }

                if (fileData.fileUri != null || fileData.fileUri.Length > 0)
                {
                    foreach (string fileUri in fileData.fileUri)
                        mShareManager.Call<AndroidJavaObject>("addFileUri", fileUri);
                }

                if (!string.IsNullOrEmpty(header))
                    mShareManager.Call<AndroidJavaObject>("setHeader", header);

                mShareManager.Call("shareMultipleFileContent");
            }


#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Share via Mail
        /// </summary>
        /// <param name="emailData">EmailSharingData which holds array of to, cc and bcc mail ids along with MultipleFilesData</param>
        /// <param name="header">share chooser header text</param>
        public void ShareOnMail(EmailSharingData emailData, string header = "")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            // Initialize Share Manager

            using (AndroidJavaClass jc = new AndroidJavaClass("com.onedevapp.nativeplugin.share.ShareManager"))
            {
                var mShareManager = jc.CallStatic<AndroidJavaObject>("Builder", mContext);

                mShareManager.Call<AndroidJavaObject>("setMessage", emailData.message);

                if (emailData.emailTo.Length > 0)
                {
                    mShareManager.Call<AndroidJavaObject>("addMultipleEmailAddress", javaArrayFromCS(emailData.emailTo));
                }

                if (emailData.emailCc.Length > 0)
                {
                    mShareManager.Call<AndroidJavaObject>("addMultipleEmailCcAddress", javaArrayFromCS(emailData.emailCc));
                }

                if (emailData.emailBcc.Length > 0)
                {
                    mShareManager.Call<AndroidJavaObject>("addMultipleEmailBccAddress", javaArrayFromCS(emailData.emailBcc));
                }

                if (emailData.fileData.filePath.Length > 0)
                {
                    mShareManager.Call<AndroidJavaObject>("addMultipleFilePaths", javaArrayFromCS(emailData.fileData.filePath));
                }

                if (emailData.fileData.fileUri.Length > 0)
                {
                    foreach (string fileUri in emailData.fileData.fileUri)
                        mShareManager.Call<AndroidJavaObject>("addFileUri", fileUri);
                }

                if (!string.IsNullOrEmpty(header))
                    mShareManager.Call<AndroidJavaObject>("setHeader", header);

                mShareManager.Call("shareOnEmail", new object[] { emailData.subject, emailData.isHtmlText });
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }
        #endregion

        #region Location
        /// <summary>
        /// Enable Location.
        /// </summary>
        public void EnableLocation()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                if (jc.CallStatic<bool>("CheckPermission", mContext, "android.permission.ACCESS_FINE_LOCATION"))
                {
                    jc.CallStatic("EnableLocation", mContext);
                }
                else
                {
                    Debug.Log("Permisson not granted");
                }
            }

#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }
        #endregion

        #region Toast
        /// <summary>
        /// Shows the toast.
        /// </summary>
        /// <param name="message">Message.</param>
        /// <param name="Length">Toast Duration, For Short 0,For Long 1.</param>
        public void ShowToast(string message, int Length = 0)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowToast", mContext, message, Length);
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }
        #endregion

        #region AlertDialogs
        /// <summary>
        /// Shows alert dialog
        /// </summary>
        /// <param name="title">Title.</param>
        /// <param name="message">Message.</param>
        /// <param name="positiveButtonName">Positive Button Name.</param>
        public void ShowAlertMessage(string title, string message, string positiveButtonName = "OK")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowAlertMessage", mContext, title, message, positiveButtonName, new OnClickPositiveListener());
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Shows confirmation dialog
        /// </summary>
        /// <param name="title">Title.</param>
        /// <param name="message">Message.</param>
        /// <param name="positiveButtonName">Positive Button Name.</param>
        /// <param name="negativeButtonName">Negative Button Name.</param>
        public void ShowConfirmationMessage(string title, string message, string positiveButtonName = "OK", string negativeButtonName = "Cancel")
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowConfirmationMessage", mContext, title, message, positiveButtonName, new OnClickPositiveListener(), negativeButtonName, new OnClickNegativeListener());
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Shows the progress bar.
        /// </summary>
        /// <param name="title">Title.</param>
        /// <param name="message">Message.</param>
        /// <param name="cancelable">If set true then on clicking outside disable ProgressBar</param>
        public void ShowProgressBar(string title, string message, bool cancelable = true)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowProgressBar", mContext, title, message, cancelable);
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Dismiss the progress bar
        /// </summary>
        public void DismissProgressBar()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("DismissProgressBar");
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Shows the time picker dialog
        /// </summary>
        public void ShowTimePicker()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowTimePicker", mContext, new OnSelectedListener());
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Shows the time picker dialog with requested hour, mins and format
        /// </summary>
        /// <param name="hour">Hour.</param>
        /// <param name="minutes">Mintues.</param>
        /// <param name="is24HourFormat">If set true then time picker dialog shows with 24 hours format else 12 hours</param>
        public void ShowTimePicker(int hour, int minutes, bool is24HourFormat)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowTimePicker", mContext, hour, minutes, is24HourFormat, new OnSelectedListener());
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Shows the date picker dialog
        /// </summary>
        public void ShowDatePicker()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowDatePicker", mContext, new OnSelectedListener());
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }

        /// <summary>
        /// Shows the date picker dialog with requested date
        /// </summary>
        /// <param name="year">Year.</param>
        /// <param name="month">Month.</param>
        /// <param name="day">Date.</param>
        public void ShowDatePicker(int year, int month, int day)
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("ShowDatePicker", mContext, year, month, day, new OnSelectedListener());
            }
#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }
        #endregion

        #region Options
        /// <summary>
        /// Check whether device is rooted or not
        /// </summary>
        public bool IsDeviceRooted()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                return jc.CallStatic<bool>("IsDeviceRooted");
            }
#elif UNITY_EDITOR
            return false;
#endif
        }

        /// <summary>
        /// Opens app settings page
        /// </summary>
        public void OpenSettingScreen()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            using (AndroidJavaClass jc = new AndroidJavaClass(m_bridgePackageName))
            {
                jc.CallStatic("OpenSettings", mContext);
            }

#elif UNITY_EDITOR
            if(writeLog)
                Debug.Log("Platform not supported");
#endif
        }
        #endregion

        #region Debug
        /// <summary>
        /// By default puglin console log will be diabled, but can be enabled
        /// </summary>
        /// <param name="showLog">If set true then log will be displayed else disabled</param>
        public void PluginDebug(bool showLog = true)
        {
#if UNITY_ANDROID && !UNITY_EDITOR

            AndroidJNIHelper.debug = showLog;
            var constantClass = new AndroidJavaClass("com.onedevapp.nativeplugin.Constants");
            constantClass.SetStatic("enableLog", showLog);

#elif UNITY_EDITOR
            writeLog = showLog;
#endif
        }
        #endregion

#if UNITY_ANDROID && !UNITY_EDITOR
        /// <summary>
        /// Converts Csharp array to java array
        /// https://stackoverflow.com/questions/42681410/androidjavaobject-call-array-passing-error-unity-for-android
        /// </summary>
        /// <param name="values"></param>
        /// <returns></returns>
        private AndroidJavaObject javaArrayFromCS(string[] values)
        {
            AndroidJavaClass arrayClass = new AndroidJavaClass("java.lang.reflect.Array");
            AndroidJavaObject arrayObject = arrayClass.CallStatic<AndroidJavaObject>("newInstance", new AndroidJavaClass("java.lang.String"), values.Length);
            for (int i = 0; i < values.Length; ++i)
            {
                arrayClass.CallStatic("set", arrayObject, i, new AndroidJavaObject("java.lang.String", values[i]));
            }

            return arrayObject;
        }
        
#endif
    }

}
