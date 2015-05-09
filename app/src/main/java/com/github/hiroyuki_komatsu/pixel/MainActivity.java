package com.github.hiroyuki_komatsu.pixel;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity {
    private PixelView mPixelView;
    private PixelData mPixelData;

    private GoogleDriveFragment mGoogleDriveFragment;
    private final String GOOGLE_DRIVE_TAG = "GoogleDrive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPixelData = new PixelData();
        mPixelView = (PixelView)findViewById(R.id.PixelView);
        mPixelView.setPixelData(mPixelData);

        mGoogleDriveFragment = new GoogleDriveFragment();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(mGoogleDriveFragment, GOOGLE_DRIVE_TAG).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("MainActivity", "onActivityResult");
        mGoogleDriveFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSaveButton(View view) {
        //mGoogleDriveFragment.saveNewFile("data.txt", "test data\n");
        //mGoogleDriveFragment.createFileActivity();
        String[] mimeType = {};
        mGoogleDriveFragment.openFile(mimeType);

        /*
        // TODO: do it in a thread.
        boolean result =
                FileOperator.saveData(this, "data.txt", "test data\n");

        if (result) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        */
    }
}
