package com.github.hiroyuki_komatsu.pixel;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by komatsu on 5/6/15.
 * Started from https://github.com/googledrive/android-demos/blob/master/src/com/google/android/gms/drive/sample/demo/BaseDemoActivity.java
 *
 * Sample code in MainActivity:
 * @@Override
 * protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     ...
 *     mGoogleDriveFragment = new GoogleDriveFragment();
 *     FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
 *     fragmentTransaction.add(mGoogleDriveFragment, GOOGLE_DRIVE_TAG).commit();
 * }
 *
 * @@Override
 * public void onActivityResult(int requestCode, int resultCode, Intent data) {
 *     super.onActivityResult(requestCode, resultCode, data);
 *
 *     mGoogleDriveFragment.onActivityResult(requestCode, resultCode, data);
 * }
 *
 * public void saveNewFile() {
 *     mGoogleDriveFragment.saveNewFile("title", "data\n");
 * }
 */
public class GoogleDriveFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleDriveFragment";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Request code for CreateFileActivity.
     */
    protected static final int REQUEST_CODE_CREATE_FILE_ACTIVITY = 2;

    /**
     * Request code for OpenFileActivity.
     */
    protected static final int REQUEST_CODE_OPEN_FILE_ACTIVITY = 3;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        }

        // For createFileActivity
        if (requestCode == REQUEST_CODE_CREATE_FILE_ACTIVITY) {
            Log.e(TAG, "REQUEST_CODE_CREATE_FILE_ACTIVITY");
            if (resultCode == Activity.RESULT_OK) {
                DriveId driveId = data.getParcelableExtra(
                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                showMessage("File created with Id: " + driveId);
            }
        }

        // For openFileActivity
        if (requestCode == REQUEST_CODE_OPEN_FILE_ACTIVITY) {
            Log.e(TAG, "REQUEST_CODE_OPEN_FILE_ACTIVITY");
            if (resultCode == Activity.RESULT_OK) {
                DriveId driveId = data.getParcelableExtra(
                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                // TODO: Need to handle multiple calls in the same time.
                if (mOpenFileCallback != null) {
                    mOpenFileCallback.driveIdCallback(driveId.encodeToString());
                    mOpenFileCallback = null;
                }
            }
        }
    }

    public interface DriveIdCallback {
        public void driveIdCallback(String driveId);
    }

    public interface DriveContentsCallback {
        public void driveContentsCallback(String driveId, String contents);
    }

    public void clearDefaultAccountAndReconnect() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        }
    }

    public void saveNewFile(String title, String data, String mimeType) {
        SaveNewFile saveNewFile = new SaveNewFile(title, data, mimeType);
        mGoogleApiClient.registerConnectionCallbacks(saveNewFile);
        mGoogleApiClient.connect();
    }

    public void createFileActivity(String title, byte[] data, String mimeType) {
        CreateFileActivity createFileActivity = new CreateFileActivity(title, data, mimeType);
        mGoogleApiClient.registerConnectionCallbacks(createFileActivity);
        mGoogleApiClient.connect();
    }

    DriveIdCallback mOpenFileCallback = null;

    public boolean openFile(String[] mimeTypes, DriveIdCallback callback) {
        if (mOpenFileCallback != null) {
            return false;
        }
        mOpenFileCallback = callback;
        OpenFileActivity openFileActivity = new OpenFileActivity(mimeTypes);
        mGoogleApiClient.registerConnectionCallbacks(openFileActivity);
        mGoogleApiClient.connect();
        return true;
    }

    public boolean getFile(String[] mimeTypes, final DriveContentsCallback callback) {
        return openFile(mimeTypes, new DriveIdCallback() {
                    @Override
                    public void driveIdCallback(String driveId) {
                        getFile(driveId, callback);
                    }
                });
    }

    public void getFile(String driveId, DriveContentsCallback callback) {
        GetFile fetchDriveId = new GetFile(driveId, callback);
        mGoogleApiClient.registerConnectionCallbacks(fetchDriveId);
        mGoogleApiClient.connect();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), getActivity(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }


    /**
     * Action class is a base class of file operations.
     */
    private abstract class Action implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.i(TAG, "GoogleApiClient connected");

            if (mGoogleApiClient.isConnectionCallbacksRegistered(thisCallbacks())) {
                Log.i(TAG, "Unregistered: " + thisCallbacks());
                mGoogleApiClient.unregisterConnectionCallbacks(thisCallbacks());
            } else {
                Log.e(TAG, "Unregister was failed: " + thisCallbacks());
            }

            action();
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.i(TAG, "GoogleApiClient connection suspended");
        }

        public void action() {
        }

        protected GoogleApiClient.ConnectionCallbacks thisCallbacks() {
            return this;
        }

        // TODO: This callback is used by SaveNewFile only.
        final protected ResultCallback<DriveFolder.DriveFileResult> mResultCallback =
                new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFileResult result) {
                        if (!result.getStatus().isSuccess()) {
                            showMessage("Error while trying to create the file");
                            return;
                        }
                        showMessage("Created a file in Root Folder: "
                                + result.getDriveFile().getDriveId());
                        Log.i(TAG, "Created a file in Root Folder: "
                                + result.getDriveFile().getDriveId());
                    }
                };
    }

    /**
     * SaveNewFile creates a new file in the root folder of Google Drive.
     */
    private class SaveNewFile extends Action {
        private String mTitle;
        private String mData;
        private String mMimeType;

        public SaveNewFile(String title, String data, String mimeType) {
            mTitle = title;
            mData = data;
            mMimeType = mimeType;
        }

        @Override
        public void action() {
            super.action();
            Drive.DriveApi.newDriveContents(getGoogleApiClient())
                    .setResultCallback(mDriveContentsCallback);
        }

        final private ResultCallback<DriveApi.DriveContentsResult> mDriveContentsCallback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            showMessage("Error while trying to create new file contents");
                            return;
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(mTitle)
                                .setMimeType(mMimeType)
                                .build();
                        DriveContents contents = result.getDriveContents();
                        OutputStream output = contents.getOutputStream();
                        Writer writer = new OutputStreamWriter(output);
                        try {
                            writer.write(mData);
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                .createFile(getGoogleApiClient(), changeSet, contents)
                                .setResultCallback(mResultCallback);
                    }
                };
    }

    private class CreateFileActivity extends Action {
        String mTitle;
        byte[] mData;
        String mMimeType;
        public CreateFileActivity(String title, byte[] data, String mimeType) {
            mTitle = title;
            mData = data;
            mMimeType = mimeType;
        }

        @Override
        public void action() {
            super.action();
            Drive.DriveApi.newDriveContents(getGoogleApiClient())
                    .setResultCallback(mDriveContentsCallback);
        }

        final private ResultCallback<DriveApi.DriveContentsResult> mDriveContentsCallback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            showMessage("Error while trying to create new file contents");
                            return;
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(mTitle)
                                .setMimeType(mMimeType)
                                .build();
                        DriveContents contents = result.getDriveContents();
                        OutputStream output = contents.getOutputStream();
                        try {
                            output.write(mData);
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // TODO: Same so far with SaveNewFile.

                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(changeSet)
                                .setInitialDriveContents(contents)
                                .build(getGoogleApiClient());
                        try {
                            getActivity().startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATE_FILE_ACTIVITY, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.w(TAG, "Failed sending intent.", e);
                        }
                    }
                };
    }

    /**
     * OpenFile shows a dialog to select a file in Google Drive.
     */
    private class OpenFileActivity extends Action {
        private String[] mMimeTypes;

        public OpenFileActivity(String[] mimeTypes) {
            mMimeTypes = mimeTypes;
        }

        @Override
        public void action() {
            super.action();
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
                    .setMimeType(mMimeTypes)
                    .build(getGoogleApiClient());
            try {
                getActivity().startIntentSenderForResult(
                        intentSender, REQUEST_CODE_OPEN_FILE_ACTIVITY, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.w(TAG, "Failed sending intent.", e);
            }
        }
    }

    /**
     * GetFile returns the contents of the DriveId.
     */
    private class GetFile extends Action {
        private String mDriveId;
        private DriveContentsCallback mCallback;

        public GetFile(String driveId, DriveContentsCallback callback) {
            mDriveId = driveId;
            mCallback = callback;
        }

        @Override
        public void action() {
            super.action();
            Drive.DriveApi.getFile(getGoogleApiClient(), DriveId.decodeFromString(mDriveId))
                    .open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(mResultIdCallback);
        }

        final private ResultCallback<DriveApi.DriveContentsResult> mResultIdCallback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            showMessage("Error while opening the file.");
                            return;
                        }

                        String contents = getContents(driveContentsResult.getDriveContents());
                        mCallback.driveContentsCallback(mDriveId, contents);
                        Log.i(TAG, "Contents: " + contents);
                    }
                };

        private String getContents(DriveContents driveContents) {
            String contents = null;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(driveContents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
            } catch (IOException e) {
                Log.e(TAG, "IOException while reading from the stream", e);
            }

            driveContents.discard(getGoogleApiClient());
            return contents;
        }
    }
}
