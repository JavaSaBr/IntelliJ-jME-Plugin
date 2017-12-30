package com.jme.example;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

/**
 * The game application class.
 */
public class GameApplication extends SimpleApplication {

    @Override
    public void simpleInitApp() {

        final Texture cubeTexture = assetManager.loadTexture("textures/jme-logo.png");

        final Material cubeMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cubeMaterial.setTexture("ColorMap", cubeTexture);

        final Box cubeMesh = new Box(1f, 1f, 1f);
        final Geometry cubeGeometry = new Geometry("My Textured Box", cubeMesh);
        cubeGeometry.setLocalTranslation(new Vector3f(-3f, 1.1f, 0f));
        cubeGeometry.setMaterial(cubeMaterial);

        rootNode.attachChild(cubeGeometry);
    }
}