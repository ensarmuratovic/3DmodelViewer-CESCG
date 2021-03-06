package org.cescg.modelviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.jme3.app.AndroidHarnessFragment;
import com.jme3.app.Application;

import org.cescg.modelviewer.Classes.Scene;
import org.cescg.modelviewer.gamelogic.Main;

/**
 * A placeholder fragment containing a simple view.
 */
public class JmeFragment extends AndroidHarnessFragment {
    private static final String TAG = "ENSAR";
    private String sceneLocalPath;
    public JmeFragment() {
        // Set main project class (fully qualified path)
        appClass = "org.cescg.modelviewer.gamelogic.Main";
        //for new activity callet from Main

        // Set the desired EGL configuration
        eglBitsPerPixel = 24;
        eglAlphaBits = 0;
        eglDepthBits = 16;
        eglSamples = 0;
        eglStencilBits = 0;

        // Set the maximum framerate
        // (default = -1 for unlimited)
        frameRate = -1;

        // Set the maximum resolution dimension
        // (the smaller side, height or width, is set automatically
        // to maintain the original device screen aspect ratio)
        // (default = -1 to match device screen resolution)
        maxResolutionDimension = -1;

        // Set input configuration settings
        joystickEventsEnabled = false;
        keyEventsEnabled = true;
        mouseEventsEnabled = true;

        // Set application exit settings
        finishOnAppStop = true;
        handleExitHook = true;
        exitDialogTitle = "Do you want to scene selection?";
        exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";

        // Set splash screen resource id, if used
        // (default = 0, no splash screen)
        // For example, if the image file name is "splash"...
          splashPicID =R.drawable.loading ;


    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            sceneLocalPath=((ViewerActivity) getActivity()).getSceneLocalPath();
            ((Main) getJmeApplication()).simpleInitApp(this); //this makes the AndroidHarness class available in SimpleApplication class
        }
        catch (Exception e)
        {
            Log.e("jmefragment greska",TAG,e);
        }
    }
    public String getSceneLocalPath() {
        return sceneLocalPath;
    }


    public void startActivity(String link){

       // Intent intent=new Intent(getActivity(),org.cescg.modelviewer.LaunchActivity.class);
       // startActivity(intent);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        startActivity(i);
    }
}
