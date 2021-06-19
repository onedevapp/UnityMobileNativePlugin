package com.onedevapp.nativeplugin.share;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import androidx.core.app.ShareCompat;

import com.onedevapp.nativeplugin.Utils;
import com.onedevapp.nativeplugin.imagepicker.ImageUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * ShareManager is the responsible class for sharing Text or Files.
 * Currently Supports Text and Image files to System Default with single or multiple files
 * Can sends to WhatApp with default or to Specific User if provided mobile no exists
 */
public class ShareManager {
    // region Declarations
    private static ShareManager instance;

    private List<Uri> mFilesUriList;  //set of all files to share
    private List<String> mEmailIdToList;  //set of all email id for share
    private List<String> mEmailIdCcList;  //set of all email id for share
    private List<String> mEmailIdBccList;  //set of all email id for share
    private String mMessage;  //message content to share
    private String mMobileNo;  //for whats app, directly send to provided mobile no
    private String mShareHeader = "Share";  //title to display on share chooser header
    private final WeakReference<Activity> mActivityWeakReference; //Activity references

    //region Constructor


    /**
     * Creates a builder
     *
     * @param activity the current activity
     * @return a new {@link ShareManager} instance
     */
    public static ShareManager Builder(Activity activity) {
        if (instance == null) {
            instance = new ShareManager(activity);
        }
        return instance;
    }

    /**
     * Creates a builder
     *
     * @param fragment the current fragment
     * @return a new {@link ShareManager} instance
     */
    public static ShareManager Builder(Fragment fragment) {
        if (instance == null) {
            instance = new ShareManager(fragment);
        }
        return instance;
    }

    //Private constructor with activity
    private ShareManager(Activity activity) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        clearData();
    }

    //Private constructor with fragment
    private ShareManager(Fragment fragment) {
        this.mActivityWeakReference = new WeakReference<>(fragment.getActivity());
        clearData();
    }

    /**
     * Returns the current activity
     */
    protected Activity getActivity() {
        return mActivityWeakReference.get();
    }
    //endregion

    private void clearData() {
        mFilesUriList = new ArrayList<>();
        mEmailIdToList = new ArrayList<>();
        mEmailIdBccList = new ArrayList<>();
        mEmailIdCcList = new ArrayList<>();
        mMessage = "";
        mMobileNo = "";
        mShareHeader = "Share";
    }

    /**
     * Set header for intent chooser
     *
     * @param headerText title message.
     * @return ShareManager itself.
     */
    public ShareManager setHeader(String headerText) {
        mShareHeader = headerText;
        return this;
    }

    /**
     * Add file path that you want to share.
     *
     * @param filePath path of the file.
     * @return ShareManager itself.
     */
    public ShareManager addFilePath(String filePath) {
        Uri fileUri = ImageUtil.getUriFromFile(getActivity(), new File(filePath));
        mFilesUriList.add(fileUri);
        return this;
    }

    /**
     * Add file uri that you want to share.
     *
     * @param fileUri path of the file.
     * @return ShareManager itself.
     */
    public ShareManager addFileUri(Uri fileUri) {
        mFilesUriList.add(fileUri);
        return this;
    }

    /**
     * Add file uri that you want to share.
     *
     * @param fileUri path of the file.
     * @return ShareManager itself.
     */
    public ShareManager addFileUri(String fileUri) {
        mFilesUriList.add(Uri.parse(fileUri));
        return this;
    }

    /**
     * All file paths that you want to share.
     *
     * @param filePaths path of the file in array.
     * @return ShareManager itself.
     */
    public ShareManager addMultipleFilePaths(String[] filePaths) {
        for (String filePath : filePaths) {
            Uri fileUri = ImageUtil.getUriFromFile(getActivity(), new File(filePath));
            mFilesUriList.add(fileUri);
        }
        return this;
    }

    /**
     * All file uris that you want to share.
     *
     * @param fileUris Uri of the file in array.
     * @return ShareManager itself.
     */
    public ShareManager addMultipleFileUris(Uri[] fileUris) {
        mFilesUriList.addAll(Arrays.asList(fileUris));
        return this;
    }

    /**
     * Message content that's need to be shared
     *
     * @param message content of the message.
     * @return ShareManager itself.
     */
    public ShareManager setMessage(String message) {
        mMessage = message;
        return this;
    }

    /**
     * To send content directly to any particular whatsapp account
     *
     * @param mobileNo mobile no with out prefix.
     * @return ShareManager itself.
     */
    public ShareManager setWhatsAppMobileNo(String mobileNo) {
        mMobileNo = mobileNo;
        return this;
    }

    /**
     * Add mail id for share.
     *
     * @param mailId Add an email addresses to be used in the "to" field
     * @return ShareManager itself.
     */
    public ShareManager addEmailAddress(String mailId) {
        mEmailIdToList.add(mailId);
        return this;
    }

    /**
     * All mail id for sharing.
     *
     * @param mailIds Add an array of email addresses to be used in the "to" field
     * @return ShareManager itself.
     */
    public ShareManager addMultipleEmailAddress(String[] mailIds) {
        mEmailIdToList.addAll(Arrays.asList(mailIds));
        return this;
    }

    /**
     * Add mail id CC for share.
     *
     * @param mailId Add an email addresses to be used in the "cc" field
     * @return ShareManager itself.
     */
    public ShareManager addEmailCcAddress(String mailId) {
        mEmailIdCcList.add(mailId);
        return this;
    }

    /**
     * All mail id CC for sharing.
     *
     * @param mailIds Add an array of email addresses to be used in the "cc" field of the final Intent
     * @return ShareManager itself.
     */
    public ShareManager addMultipleEmailCcAddress(String[] mailIds) {
        mEmailIdCcList.addAll(Arrays.asList(mailIds));
        return this;
    }

    /**
     * Add mail id BCC for share.
     *
     * @param mailId Add an email addresses to be used in the "bcc" field
     * @return ShareManager itself.
     */
    public ShareManager addEmailBccAddress(String mailId) {
        mEmailIdBccList.add(mailId);
        return this;
    }

    /**
     * All mail id BCC for sharing.
     *
     * @param mailIds Add an array of email addresses to be used in the "bcc" field
     * @return ShareManager itself.
     */
    public ShareManager addMultipleEmailBccAddress(String[] mailIds) {
        mEmailIdBccList.addAll(Arrays.asList(mailIds));
        return this;
    }

    /**
     * Generic intent for text and file sharing
     *
     * @return intent
     */
    private Intent getGenericFileIntent() {
        return ShareCompat.IntentBuilder.from(getActivity())
                .setStream(mFilesUriList.get(0)) // uri from FileProvider
                .setType("text/html")
                .setText(mMessage)
                .getIntent()
                .setAction(Intent.ACTION_SEND) //Change if needed
                .setDataAndType(mFilesUriList.get(0), "*/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    /**
     * Generic intent for text with multiple file sharing
     *
     * @return intent
     */
    private Intent getGenericMultipleFileIntent() {
        ShareCompat.IntentBuilder sendIntentBuilder = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/html")
                .setText(mMessage)
                .setType("*/*");

        for (Uri fileUri : mFilesUriList) {
            sendIntentBuilder.addStream(fileUri);
        }
        return sendIntentBuilder
                .getIntent()
                .setAction(Intent.ACTION_SEND_MULTIPLE) //Change if needed
                .setDataAndType(mFilesUriList.get(0), "*/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    /**
     * Share text via intent
     */
    public void shareTextContent() {
        ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/html")
                .setText(mMessage)
                .setChooserTitle(mShareHeader)
                .startChooser();
        clearData();
    }

    /**
     * Share text and file content via intent
     */
    public void shareFileContent() {
        if (mFilesUriList.isEmpty()) return;

        Intent sendIntent = getGenericFileIntent();
        Intent shareIntent = Intent.createChooser(sendIntent, mShareHeader);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getActivity().startActivity(shareIntent);
        clearData();
    }

    /**
     * Share text and multiple file content via intent
     */
    public void shareMultipleFileContent() {
        if (mFilesUriList.isEmpty()) return;

        Intent sendIntent = getGenericMultipleFileIntent();
        Intent shareIntent = Intent.createChooser(sendIntent, mShareHeader);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        /*List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            getActivity().grantUriPermission(packageName, ImageUtil.getUriFromFile(getActivity(), ), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }*/
        getActivity().startActivity(shareIntent);
        clearData();
    }

    /**
     * Share text and file content via whatsapp
     */
    public void shareOnWhatsApp() {
        Intent sendIntent = getGenericFileIntent();
        sendIntent.setPackage("com.whatsapp");
        sendIntent.setType("text/plain");

        if (!TextUtils.isEmpty(mMobileNo))
            sendIntent.putExtra("jid", mMobileNo + "@s.whatsapp.net"); //phone number without "+" prefix

        try {
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Objects.requireNonNull(getActivity()).startActivity(sendIntent);
            clearData();
        } catch (android.content.ActivityNotFoundException ex) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.whatsapp")));
        }
    }

    /**
     * Share text and file content via email
     *
     * @param subject    text content for subject
     * @param isHtmlText bool whether text is HTML or plain
     */
    public void shareOnEmail(String subject, boolean isHtmlText) {
        ShareCompat.IntentBuilder sendIntentBuilder = ShareCompat.IntentBuilder.from(getActivity());

        if (!mEmailIdToList.isEmpty())
            sendIntentBuilder.addEmailTo(mEmailIdToList.toArray(new String[0]));
        if (!mEmailIdCcList.isEmpty())
            sendIntentBuilder.addEmailCc(mEmailIdCcList.toArray(new String[0]));
        if (!mEmailIdBccList.isEmpty())
            sendIntentBuilder.addEmailBcc(mEmailIdBccList.toArray(new String[0]));

        for (Uri fileUri : mFilesUriList) {
            sendIntentBuilder.addStream(fileUri);
        }

        if (isHtmlText) {
            sendIntentBuilder.setType("text/html");
            sendIntentBuilder.setHtmlText(mMessage);
        } else {
            sendIntentBuilder.setType("text/plain");
            sendIntentBuilder.setText(mMessage);
        }

        Intent sendIntent = sendIntentBuilder.setSubject(subject)
                .getIntent();

        if (!mFilesUriList.isEmpty())
            sendIntent
                    .setType("*/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        getActivity().startActivity(sendIntent);
        clearData();
    }
}
