package org.jmonkeyengine.simple_jme_android.gamelogic;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.scene.plugins.OBJLoader;

import java.util.logging.Logger;

import static com.jme3.app.R.layout.main;

/**
 * Created by potterec on 3/17/2016.
 */
public class Main extends SimpleApplication {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public void simpleInitApp() {



        //adding sun
        DirectionalLight sun=new DirectionalLight();
        sun.setDirection(new Vector3f(0.69077975f,-0.6277887f,-0.35875428f).normalizeLocal());
        sun.setColor(ColorRGBA.White.clone().multLocal(2));
        rootNode.addLight(sun);

        //adding ambient light
        AmbientLight al=new AmbientLight();
        rootNode.addLight(al);

        //doesent work
        //Spatial model2=assetManager.loadModel("Models/untitlednovo.obj");
        //rootNode.attachChild(model2);

        //EXAMPLE WITH ONLINE MODEL

       // assetManager.registerLocator("http://www85.zippyshare.com/d/aiiYXtDl/560868/town.zip",HttpZipLocator.class);
        //Spatial model3=assetManager.loadModel("main.scene");

        //rootNode.attachChild(model3);


        //EXAMPLE WITH SIMPLE BOX
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        Box box = new Box(1, 1, 1);
        Geometry geom = new Geometry("box", box);
        geom.setMaterial(mat);
        Texture texture = assetManager.loadTexture("Textures/Monkey.png");
        mat.setTexture("ColorMap", texture);
        rootNode.attachChild(geom);


        // Disable the default flyby cam
        flyCam.setEnabled(false);
        // Enable a chase cam for model, and set object for center of rotation
        ChaseCamera chaseCam = new ChaseCamera(cam, geom, inputManager);
        chaseCam.setSmoothMotion(true);

    }
}
