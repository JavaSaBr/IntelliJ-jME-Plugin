package com.ss.jme.plugin.framework.support;

import static com.ss.jme.plugin.util.JmePluginUtils.getOrCreateFile;
import static com.ss.jme.plugin.util.JmePluginUtils.getOrCreateFolders;
import com.intellij.framework.FrameworkTypeEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.ss.rlib.common.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.frameworkSupport.BuildScriptDataBuilder;
import org.jetbrains.plugins.gradle.frameworkSupport.GradleFrameworkSupportProvider;

import java.io.IOException;

/**
 * The provider to create a jMonkeyEngine gradle build file with some additional files.
 *
 * @author JavaSaBr
 */
public class JmeFrameworkSupportProvider extends GradleFrameworkSupportProvider {

    private static final Logger LOG = Logger.getInstance("#com.ss.jme.plugin.framework.support.JmeFrameworkSupportProvider");

    private static final String NATIVE_BUILD_SCRIPT_PATH = "com/ss/jme/plugin/project/template/build-native.xml";
    private static final String LOGO_IMAGE_PATH = "com/ss/jme/plugin/project/template/jme-logo.png";
    private static final String SIMPLE_SCENE_PATH = "com/ss/jme/plugin/project/template/SimpleScene.j3s";
    private static final String TERRAIN_PATH = "com/ss/jme/plugin/project/template/ground03.tga";
    private static final String TERRAIN_ALPHA_1_PATH = "com/ss/jme/plugin/project/template/terrain-alpha-blend-1.png";
    private static final String TERRAIN_ALPHA_2_PATH = "com/ss/jme/plugin/project/template/terrain-alpha-blend-2.png";
    private static final String TERRAIN_ALPHA_3_PATH = "com/ss/jme/plugin/project/template/terrain-alpha-blend-3.png";
    private static final String GAME_APPLICATION_PATH = "com/ss/jme/plugin/project/template/GameApplication.java";
    private static final String STARTER_PATH = "com/ss/jme/plugin/project/template/Starter.java";

    private static final String NATIVE_BUILD_SCRIPT;
    private static final String GAME_APPLICATION;
    private static final String STARTER;

    private static final byte[] LOGO_IMAGE;
    private static final byte[] SIMPLE_SCENE;
    private static final byte[] TERRAIN;
    private static final byte[] TERRAIN_ALPHA_1;
    private static final byte[] TERRAIN_ALPHA_2;
    private static final byte[] TERRAIN_ALPHA_3;

    static {
        try {
            final ClassLoader classLoader = JmeFrameworkSupportProvider.class.getClassLoader();
            LOG.debug("read " + NATIVE_BUILD_SCRIPT_PATH);
            NATIVE_BUILD_SCRIPT = FileUtils.read(classLoader.getResourceAsStream(NATIVE_BUILD_SCRIPT_PATH));
            LOG.debug("read " + GAME_APPLICATION_PATH);
            GAME_APPLICATION = FileUtils.read(classLoader.getResourceAsStream(GAME_APPLICATION_PATH));
            LOG.debug("read " + STARTER_PATH);
            STARTER = FileUtils.read(classLoader.getResourceAsStream(STARTER_PATH));
            LOG.debug("read " + LOGO_IMAGE_PATH);
            LOGO_IMAGE = IOUtils.toByteArray(classLoader.getResourceAsStream(LOGO_IMAGE_PATH));
            LOG.debug("read " + SIMPLE_SCENE_PATH);
            SIMPLE_SCENE = IOUtils.toByteArray(classLoader.getResourceAsStream(SIMPLE_SCENE_PATH));
            LOG.debug("read " + TERRAIN_PATH);
            TERRAIN = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_PATH));
            LOG.debug("read " + TERRAIN_ALPHA_1_PATH);
            TERRAIN_ALPHA_1 = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_ALPHA_1_PATH));
            LOG.debug("read " + TERRAIN_ALPHA_2_PATH);
            TERRAIN_ALPHA_2 = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_ALPHA_2_PATH));
            LOG.debug("read " + TERRAIN_ALPHA_3_PATH);
            TERRAIN_ALPHA_3 = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_ALPHA_3_PATH));
        } catch (IOException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public JmeFrameworkSupportProvider() {
    }

    @Override
    public @NotNull FrameworkTypeEx getFrameworkType() {
        return new JmeFrameworkTypeEx("jMonkeyEngine", this);
    }

    @Override
    public void addSupport(
            @NotNull ProjectId projectId,
            @NotNull Module module,
            @NotNull ModifiableRootModel rootModel,
            @NotNull ModifiableModelsProvider modifiableModelsProvider,
            @NotNull BuildScriptDataBuilder buildScriptData
    ) {

        buildScriptData
            .addRepositoriesDefinition("jcenter()")
            .addRepositoriesDefinition("mavenCentral()")
            .addRepositoriesDefinition("maven { url \"https://jitpack.io\" }")
            .addRepositoriesDefinition("maven { url \"https://dl.bintray.com/stephengold/jme3utilities\" }")
            .addRepositoriesDefinition("maven { url \"http://dl.bintray.com/jmonkeyengine/org.jmonkeyengine\" }")

            .addPluginDefinition("apply plugin: 'maven'")
            .addPluginDefinition("apply plugin: 'java'")
            .addPluginDefinition("apply plugin: 'application'")

            .addPropertyDefinition("sourceCompatibility = 1.8")
            .addPropertyDefinition("targetCompatibility = 1.8")
            .addPropertyDefinition("" +
                "// replace resources folder to asset folder\n" +
                "sourceSets {\n" +
                "    main {\n" +
                "        resources {\n" +
                "            srcDirs= [\"src/main/assets\"]\n" +
                "        }\n" +
                "    }\n" +
                "}\n\n" +
                "// additional properties for native build \n" +
                "ext.applicationTitle = \"jME example\"\n" +
                "ext.applicationVendor = \"No vendor\"\n" +
                "ext.applicationMainClass = \"com.jme.example.Starter\"\n" +
                "mainClassName = ext.applicationMainClass\n\n" +
                "// define version of jME\n" +
                "def jme3 = [v: '3.2.1-stable', g: 'org.jmonkeyengine']\n")
            .addDependencyNotation("compile \"${jme3.g}:jme3-core:${jme3.v}\"")
            .addDependencyNotation("compile \"${jme3.g}:jme3-effects:${jme3.v}\"")
            .addDependencyNotation("compile \"${jme3.g}:jme3-bullet:${jme3.v}\"")
            .addDependencyNotation("compile \"${jme3.g}:jme3-bullet-native:${jme3.v}\"")
            .addDependencyNotation("compile \"${jme3.g}:jme3-terrain:${jme3.v}\"")
            .addDependencyNotation("runtime \"${jme3.g}:jme3-plugins:${jme3.v}\"")
            .addDependencyNotation("runtime \"${jme3.g}:jme3-lwjgl3:${jme3.v}\"")
            .addDependencyNotation("runtime \"${jme3.g}:jme3-jogg:${jme3.v}\"")
            .addDependencyNotation("runtime \"${jme3.g}:jme3-desktop:${jme3.v}\"")

            .addDependencyNotation("compile 'com.github.JavaSaBr:jmonkeybuilder-extension:2.1.1'")
            .addDependencyNotation("compile 'com.github.JavaSaBr:tonegodemitter:2.4.1'")
            .addDependencyNotation("compile 'jme3utilities:SkyControl:0.9.12'")

            .addOther("" +
                "ant.importBuild('build-native.xml')\n" +
                "ant.basedir = new File(buildDir.getParentFile(), \"native-build\")\n" +
                "ant.properties.javaSourceVersion = sourceCompatibility\n" +
                "ant.properties.javaTargetVersion = targetCompatibility\n" +
                "ant.properties.applicationJarName = name + \".jar\"\n" +
                "ant.properties.applicationMainClass = ext.applicationMainClass\n" +
                "ant.properties.applicationVendor = ext.applicationVendor\n" +
                "ant.properties.applicationTitle = ext.applicationTitle\n" +
                "ant.properties.applicationVersion = version\n\n" +
                "// a task to prepare dependencies for native build\n" +
                "task prepareDependencies(type: Copy) {\n" +
                "\n" +
                "    doFirst {\n" +
                "        println(\"delete \" + \"$buildDir/dependencies\")\n" +
                "        GFileUtils.deleteDirectory(new File(\"$buildDir/dependencies\"))\n" + "    }\n" +
                "\n" +
                "    into \"$buildDir/dependencies\"\n" +
                "    from configurations.runtime\n" +
                "}\n\n" +
                "task wrapper(type: Wrapper) {\n" +
                "    gradleVersion = '4.6'\n" +
                "}");

        Project project = module.getProject();
        VirtualFile baseDir = project.getBaseDir();

        ApplicationManager.getApplication()
                .runWriteAction(() -> createAdditionalFiles(baseDir));
    }

    /**
     * Creates additional files to new project.
     *
     * @param baseDir the base dir of the project.
     */
    private void createAdditionalFiles(@NotNull VirtualFile baseDir) {
        try {

            // creates the native build script
            VirtualFile nativeBuildFile = getOrCreateFile(baseDir,this, "build-native.xml");

            VfsUtil.saveText(nativeBuildFile, NATIVE_BUILD_SCRIPT);

            // creates example classes
            VirtualFile mainDir = getOrCreateFolders(baseDir,this,
                    "src", "main");

            VirtualFile assetsDir = getOrCreateFolders(mainDir, this, "assets");

            getOrCreateFolders(assetsDir, this, "Models");
            getOrCreateFolders(assetsDir, this, "Sounds");
            getOrCreateFolders(assetsDir, this, "Interface");
            getOrCreateFolders(assetsDir, this, "MatDefs");
            getOrCreateFolders(assetsDir, this, "Materials");
            getOrCreateFolders(assetsDir, this, "Shaders");

            VirtualFile texturesDir = getOrCreateFolders(assetsDir, this, "Textures");

            getOrCreateFile(texturesDir, this, "jme-logo.png")
                    .setBinaryContent(LOGO_IMAGE);

            VirtualFile terrainAlphaDir = getOrCreateFolders(texturesDir, this, "TerrainAlpha");

            getOrCreateFile(terrainAlphaDir, this, "terrain-alpha-blend-1.png")
                    .setBinaryContent(TERRAIN_ALPHA_1);
            getOrCreateFile(terrainAlphaDir, this, "terrain-alpha-blend-2.png")
                    .setBinaryContent(TERRAIN_ALPHA_2);
            getOrCreateFile(terrainAlphaDir, this, "terrain-alpha-blend-3.png")
                    .setBinaryContent(TERRAIN_ALPHA_3);

            VirtualFile terrainDir = getOrCreateFolders(texturesDir, this, "Terrain");

            getOrCreateFile(terrainDir, this, "ground03.tga")
                    .setBinaryContent(TERRAIN);

            VirtualFile scenesDir = getOrCreateFolders(assetsDir, this, "Scenes");

            getOrCreateFile(scenesDir, this, "SimpleScene.j3s")
                    .setBinaryContent(SIMPLE_SCENE);

            VirtualFile exampleDir = getOrCreateFolders(mainDir, this,
                    "java", "com", "jme", "example");

            VirtualFile gameApplicationFile = getOrCreateFile(exampleDir, this, "GameApplication.java");
            VirtualFile starterFile = getOrCreateFile(exampleDir, this, "Starter.java");

            VfsUtil.saveText(gameApplicationFile, GAME_APPLICATION);
            VfsUtil.saveText(starterFile, STARTER);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
