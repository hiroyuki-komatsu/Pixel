package com.github.hiroyuki_komatsu.pixel;

import android.app.Activity;
import android.app.Fragment;
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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
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
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    public void clearDefaultAccountAndReconnect() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        }
    }

    public void saveNewFile(String title, String data) {
        SaveNewFile saveNewFile = new SaveNewFile(title, data);
        mGoogleApiClient.registerConnectionCallbacks(saveNewFile);
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

        final protected ResultCallback<DriveFolder.DriveFileResult> mResultCallback =
                new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFileResult result) {
                        if (!result.getStatus().isSuccess()) {
                            showMessage("Error while trying to create the file");
                            return;
                        }
                        showMessage("Created a file in App Folder: "
                                + result.getDriveFile().getDriveId());
                        Log.i(TAG, "Created a file in App Folder: "
                                + result.getDriveFile().getDriveId());
                        if (mGoogleApiClient.isConnectionCallbacksRegistered(thisCallbacks())) {
                            Log.i(TAG, "Unregistered: " + thisCallbacks());
                            mGoogleApiClient.unregisterConnectionCallbacks(thisCallbacks());
                        } else {
                            Log.e(TAG, "Unregister was failed: " + thisCallbacks());
                        }
                    }
                };
    }

    /**
     * SaveNewFile crates a new file in the root folder of Google Drive.
     */
    private class SaveNewFile extends Action {
        private String mTitle;
        private String mData;

        public SaveNewFile(String title, String data) {
            mTitle = title;
            mData = data;
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
                                .setMimeType("text/plain")
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
}
