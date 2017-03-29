package org.cescg.modelviewer;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jme3.app.AndroidHarnessFragment;
import com.jme3.app.Application;

import org.cescg.modelviewer.gamelogic.Main;

/**
 * A placeholder fragment containing a simple view.
 */
public class JmeFragment extends AndroidHarnessFragment {
    private static final String TAG = "ENSAR";
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
        exitDialogTitle = "Do you want to exit?";
        exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";

        // Set splash screen resource id, if used
        // (default = 0, no splash screen)
        // For example, if the image file name is "splash"...
        //     splashPicID = R.drawable.splash;
        splashPicID = 0;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Main)getJmeApplication()).simpleInitApp(this); //this makes the AndroidHarness class available in SimpleApplication class
    }

    public void startActivity(){
        //Intent intent1 = new Intent(myActivity, com.example.Interferenz.InterferenzActivity.class);
        //intent1.putExtra("objekt",(byte)1);
        //myActivity.startActivity(intent1);
        Intent intent=new Intent(getActivity(),org.cescg.modelviewer.LaunchActivity.class);
        startActivity(intent);
        Log.i(TAG, "uspjeloo");
    }



}
