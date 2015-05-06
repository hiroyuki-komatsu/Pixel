package com.github.hiroyuki_komatsu.pixel;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by komatsu on 5/6/15.
 */
public class FileOperator {
    public FileOperator() {
    }

    public static boolean saveData(Activity activity, String fileName, String data) {
        OutputStream out;
        try {
            out = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.append(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
