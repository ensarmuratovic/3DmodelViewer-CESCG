package org.cescg.modelviewer.gamelogic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.plugins.AndroidLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.export.JmeImporter;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.input.ChaseCamera;
import com.jme3.input.FlyByCamera;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.plugins.MTLLoader;
import com.jme3.scene.plugins.fbx.FbxLoader;


import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.scene.plugins.OBJLoader;
import com.jme3.util.SkyFactory;

import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;

import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.event.BasePickState;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.event.MouseEventControl;
import com.jme3.scene.control.AbstractControl;
import com.simsilica.lemur.style.BaseStyles;
import com.jme3.export.JmeImporter;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import org.cescg.modelviewer.JmeFragment;
import org.slf4j.Marker;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jme3.app.R.layout.main;


public class Main extends SimpleApplication {
    private ChaseCamera chaseCam;

    Button forwardButton;
    Button backwardButton;
    Button rightButton;
    Button leftButton;
    Button switchCameraButton;

    Spatial mainObject;
    Geometry marker;
    Vector3f initFocus;
    PointLight point;
    private JmeFragment jmeFragment;
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final String TAG = "ENSAR";

    public void simpleInitApp(JmeFragment jme)
    {
        jmeFragment=jme;
    }
    public void simpleInitApp() {
         assetManager.registerLoader(OBJLoader.class, "obj");
         assetManager.registerLoader(MTLLoader.class, "mtl");
        //setDisplayFps(false);
       // setDisplayStatView(false);
        try {

            /*DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(0.69077975f,-0.6277887f,-0.35875428f).normalizeLocal());
            sun.setColor(ColorRGBA.White.clone().multLocal(2));
            rootNode.addLight(sun);*/
            //adding ambient light
            // AmbientLight al = new AmbientLight();
            // al.setColor(ColorRGBA.White.mult(0.9f));
            // al.setEnabled(true);
            // rootNode.addLight(al);
            rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/sky.jpg", true));
            //assetManager.registerLocator("/storage/emulated/0/download/proba", AndroidLocator.class);
            //assetManager.registerLocator("/storage/emulated/0/download/proba", FileLocator.class);
            assetManager.registerLocator("/storage/emulated/0/"+jmeFragment.getSceneLocalPath(), FileLocator.class);
             mainObject=assetManager.loadModel("blendexp10k.obj");

            point = new PointLight();
            point.setColor(ColorRGBA.White.mult(2f));
            //point.setPosition(new Vector3f(mainObject.getLocalTranslation().getX(),mainObject.getLocalTranslation().getY()+2f,mainObject.getLocalTranslation().getZ()));
            point.setPosition(cam.getLocation());
            rootNode.addLight(point);
        /* BinaryExporter ex = BinaryExporter.getInstance();

        File f = new File("/storage/emulated/0/download/model.j3o");

        try {

            ex.save(mainObject , f);

            System.out.println("File was successfully converted to j3o.");



        } catch (IOException e) {

            e.printStackTrace();

        }*/


            //EXAMPLE WITH SIMPLE BOX
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Blue);
            Box box = new Box(1, 1, 1);
            //  mainObject = new Geometry("box", box);
            // mainObject.setMaterial(mat);
            Texture texture = assetManager.loadTexture("Textures/Monkey.png");
            mat.setTexture("ColorMap", texture);
            //rootNode.attachChild(mainObject);
           // mainObject = new Geometry("box", box);
           // mainObject.setMaterial(mat);

            rootNode.attachChild(mainObject);
            cam.lookAt(new Vector3f( mainObject.getLocalTranslation()),new Vector3f(0,1,0));
            marker = new Geometry("box", box);
            marker.setMaterial(mat);
            marker.scale(0.2f);
            // marker.scale(((BoundingBox) mainObject.getWorldBound()).getXExtent()/5);
            marker.move(mainObject.getLocalTranslation().getX(), mainObject.getLocalTranslation().getY() + 3f, mainObject.getLocalTranslation().getZ());
            rootNode.attachChild(marker);

            initializeCameras();
            initializeGui();

            Log.i("cetvrto:" + jmeFragment.getSceneLocalPath(), TAG);


            //listener of click on object
            AppStateManager appStateManager = new AppStateManager(this);
            appStateManager.attach(new MouseAppState(this));
            MouseEventControl.addListenersToSpatial(marker, new DefaultMouseListener() {
                @Override
                protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                    Log.i(TAG, "clicked box.");
                    jmeFragment.startActivity();

                }
            });

        /*forward.addMouseListener(new DefaultMouseListener() {


            @Override
            public void mouseEntered(MouseMotionEvent event, Spatial target, Spatial capture) {
                super.mouseEntered(event, target, capture);
                chaseCam.setMaxDistance(chaseCam.getDistanceToTarget()-1f);
            }
        });*/

        }
        catch (Exception e) {
            Log.e("JME greska",TAG,e);
        }

    }
    //tpf=time per frame
    @Override
   public void simpleUpdate(float tpf) {
        // make the player rotate:
         marker.rotate(0,-tpf,0);
        point.setPosition(cam.getLocation());
     if(forwardButton.isPressed()==true) {
            if(chaseCam.isEnabled()) {
                 chaseCam.setDefaultDistance(chaseCam.getDistanceToTarget() - tpf * 7);
                return;
            }
         cam.setLocation(new Vector3f(cam.getLocation().getX()-tpf*7,cam.getLocation().getY(),cam.getLocation().getZ()));
        }

        if(backwardButton.isPressed()==true ) {
            if(chaseCam.isEnabled()) {
                chaseCam.setDefaultDistance(chaseCam.getDistanceToTarget() + tpf * 7);
                return;
            }

            cam.setLocation(new Vector3f(cam.getLocation().getX()+tpf*7,cam.getLocation().getY(),cam.getLocation().getZ()));
        }
        if(rightButton.isPressed()==true) {
            if(chaseCam.isEnabled()) {
                //cam.setLocation(new Vector3f(cam.getLocation().getX()+tpf*7,cam.getLocation().getY(),cam.getLocation().getZ()));
                //mainObject.setLocalTranslation(mainObject.getLocalTranslation().getX(),mainObject.getLocalTranslation().getY()+tpf*7,mainObject.getLocalTranslation().getZ());
                mainObject.rotate(0,tpf,0);
            }


           // marker.setLocalTranslation(marker.getLocalTranslation().getX(),marker.getLocalTranslation().getY(),marker.getLocalTranslation().getZ()+7*tpf);
            cam.setLocation(new Vector3f(cam.getLocation().getX() , cam.getLocation().getY(), cam.getLocation().getZ()+5*tpf));


        }
        if(leftButton.isPressed()==true) {
            if(chaseCam.isEnabled()) {
                //mainObject.setLocalTranslation(mainObject.getLocalTranslation().getX(),mainObject.getLocalTranslation().getY()-tpf-7,mainObject.getLocalTranslation().getZ());
                mainObject.rotate(0,-tpf,0);
            }
           // marker.setLocalTranslation(marker.getLocalTranslation().getX(),marker.getLocalTranslation().getY(),marker.getLocalTranslation().getZ()-7*tpf);
            cam.setLocation(new Vector3f(cam.getLocation().getX(), cam.getLocation().getY(), cam.getLocation().getZ() -5*tpf));
        }

    }

    public void initializeCameras()
    {
         flyCam.setEnabled(false);
        cam.setParallelProjection(false);
        //cam.lookAt(new Vector3f( mainObject.getLocalTranslation()),new Vector3f(0,1,0));

      //Enable a chase cam for model, and set object for center of rotation
        chaseCam = new ChaseCamera(cam, mainObject, inputManager);
        chaseCam.setTrailingEnabled(true);
        chaseCam.setSmoothMotion(true);
        chaseCam.setMaxDistance(30f);
        chaseCam.setMinDistance(0.1f);
        chaseCam.setDefaultDistance(15f);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setChasingSensitivity(5);
        chaseCam.setZoomSensitivity(3);
        initFocus=chaseCam.getLookAtOffset();


    }
    public void initializeGui(){
        GuiGlobals.initialize(this);
        Node cursors=new Node();
        guiNode.attachChild(cursors);
        guiNode.setLocalTranslation(100,100,0);

        Container forwardWindow = new Container();
        cursors.attachChild(forwardWindow);
        forwardWindow.setLocalTranslation(50, 200, 0);

        Container backwardWindow = new Container();
        cursors.attachChild(backwardWindow);
        backwardWindow.setLocalTranslation(50, 0, 0);

        Container rightWindow = new Container();
        cursors.attachChild(rightWindow);
        rightWindow.setLocalTranslation(145, 100, 0);

        Container leftWindow = new Container();
        cursors.attachChild(leftWindow);
        leftWindow.setLocalTranslation(-50, 100, 0);

        Container switchCameraWindow = new Container();
        cursors.attachChild(switchCameraWindow);
        switchCameraWindow.setLocalTranslation(-50, 290, 0);

        forwardButton  = forwardWindow.addChild(new Button(""));
        IconComponent forwardIco=new IconComponent("assets/Interface/forward.png");
        forwardIco.setIconScale(2f);
        forwardButton.setIcon(forwardIco);


        backwardButton = backwardWindow.addChild(new Button(""));
        IconComponent backwardIco=new IconComponent("assets/Interface/backward.png");
        backwardIco.setIconScale(2f);
        backwardButton.setIcon(backwardIco);


        rightButton = rightWindow.addChild(new Button(""));
        IconComponent rightIco=new IconComponent("assets/Interface/right.png");
        rightIco.setIconScale(2f);
        rightButton.setIcon(rightIco);

        leftButton = leftWindow.addChild(new Button(""));
        IconComponent leftIco=new IconComponent("assets/Interface/left.png");
        leftIco.setIconScale(2f);
        leftButton.setIcon(leftIco);

        switchCameraButton = switchCameraWindow.addChild(new Button("Enable camera move"));
        switchCameraButton.setFontSize(30f);
        QuadBackgroundComponent bg = new QuadBackgroundComponent(ColorRGBA.White);
        switchCameraWindow.setBackground(bg);
        switchCameraButton.setHighlightColor(ColorRGBA.Red);
        switchCameraButton.setColor(ColorRGBA.Black);

        switchCameraButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                if(switchCameraButton.getText().equals("Enable model rotation"))
                    switchCameraButton.setText("Enable camera move");
                else
                    switchCameraButton.setText("Enable model rotation");
                if(chaseCam.isEnabled()) {
                    flyCam.setEnabled(true);
                    chaseCam.setEnabled(false);
                    return;
                }
                flyCam.setEnabled(false);
                chaseCam.setEnabled(true);
                chaseCam.setLookAtOffset(initFocus);


            }
        });
    }

}
