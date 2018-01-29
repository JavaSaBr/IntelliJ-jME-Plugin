package com.jme.example;

import com.jme3.app.SimpleApplication;
import com.jme3.material.TechniqueDef;
import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.post.FilterPostProcessor;
import com.ss.editor.extension.loader.SceneLoader;

/**
 * The game application class.
 */
public class GameApplication extends SimpleApplication {

    /**
     * The post filter processor.
     */
    protected FilterPostProcessor postProcessor;

    @Override
    public void simpleInitApp() {
        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePass);
        renderManager.setSinglePassLightBatchSize(5);

        postProcessor = new FilterPostProcessor(assetManager);
        postProcessor.initialize(renderManager, viewPort);

        // register post effects filter
        viewPort.addProcessor(postProcessor);

        // register loader of j3s files
        SceneLoader.install(this, postProcessor);

        final Camera camera = getCamera();
        camera.setLocation(new Vector3f(99.50714F, 19.356062F, 44.070957F));
        camera.setRotation(new Quaternion(-0.042982846F, 0.90933293F, -0.09716145F, -0.40227568F));

        getFlyByCamera().setMoveSpeed(5);

        rootNode.attachChild(assetManager.loadModel("Scenes/SimpleScene.j3s"));
    }
}