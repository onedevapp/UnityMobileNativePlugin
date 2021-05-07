using System;

namespace OneDevApp
{

/// <summary>
/// ImageData class model for sharing image details
/// </summary>
[Serializable]
public class ImageData
{
    public bool status;
    public string message;
    public int errorCode;
    public int width;
    public int height;
    public string uri;
    public string path;
    public string imageBase64;
}
/// <summary>
/// EmailSharingData class model for sharing email content details
/// </summary>
[Serializable]
public class EmailSharingData
{
    public string message;
    public bool isHtmlText = false;
    public string subject;
    public string[] emailTo;
    public string[] emailCc;
    public string[] emailBcc;
    public MultipleFilesData fileData;

    public EmailSharingData()
    {
        emailTo = new string[0];
        emailCc = new string[0];
        emailBcc = new string[0];
        fileData = new MultipleFilesData();
    }
}
/// <summary>
/// MultipleFilesData class model to share multiple image files
/// </summary>
[Serializable]
public class MultipleFilesData
{
    public string[] fileUri;
    public string[] filePath;

    public MultipleFilesData()
    {
        fileUri = new string[0];
        filePath = new string[0];
    }
}

}