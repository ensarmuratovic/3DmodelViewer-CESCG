package org.cescg.modelviewer;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ViewerActivity extends AppCompatActivity {
     private String sceneLocalPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The "activity_main" layout includes the reference
        // to the fragment that contains the GLSurfaceView
        // that will be used to display the jME content.
        sceneLocalPath=getIntent().getStringExtra("sceneLocalPath");
        setContentView(R.layout.activity_main);
    }
    public String getSceneLocalPath() {
        return sceneLocalPath;
    }


}
