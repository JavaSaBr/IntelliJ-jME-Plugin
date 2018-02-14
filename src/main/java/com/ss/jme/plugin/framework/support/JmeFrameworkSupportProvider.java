package com.ss.jme.plugin.framework.support;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.ss.rlib.util.FileUtils;
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

    @NotNull
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
            NATIVE_BUILD_SCRIPT = FileUtils.read(classLoader.getResourceAsStream(NATIVE_BUILD_SCRIPT_PATH));
            GAME_APPLICATION = FileUtils.read(classLoader.getResourceAsStream(GAME_APPLICATION_PATH));
            STARTER = FileUtils.read(classLoader.getResourceAsStream(STARTER_PATH));
            LOGO_IMAGE = IOUtils.toByteArray(classLoader.getResourceAsStream(LOGO_IMAGE_PATH));
            SIMPLE_SCENE = IOUtils.toByteArray(classLoader.getResourceAsStream(SIMPLE_SCENE_PATH));
            TERRAIN = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_PATH));
            TERRAIN_ALPHA_1 = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_ALPHA_1_PATH));
            TERRAIN_ALPHA_2 = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_ALPHA_2_PATH));
            TERRAIN_ALPHA_3 = IOUtils.toByteArray(classLoader.getResourceAsStream(TERRAIN_ALPHA_3_PATH));
        } catch (final IOException e) {
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
    public void addSupport(@NotNull final ProjectId projectId, @NotNull final Module module,
                           @NotNull final ModifiableRootModel rootModel,
                           @NotNull final ModifiableModelsProvider modifiableModelsProvider,
                           @NotNull final BuildScriptDataBuilder buildScriptData) {

        buildScriptData

            .addRepositoriesDefinition("jcenter()")
            .addRepositoriesDefinition("mavenCentral()")
            .addRepositoriesDefinition("maven { url 'https://jitpack.io' }")
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

            .addDependencyNotation("compile 'com.github.JavaSaBr:jmonkeybuilder-extension:1.9.8'")
            .addDependencyNotation("compile 'com.github.JavaSaBr:tonegodemitter:2.4.1'")
            .addDependencyNotation("compile 'jme3utilities:SkyControl:0.9.11'")

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
                "    gradleVersion = '4.4'\n" +
                "}");

        final Project project = module.getProject();
        final VirtualFile baseDir = project.getBaseDir();

        final Application application = ApplicationManager.getApplication();
        application.runWriteAction(() -> createAdditionalFiles(baseDir));
    }

    /**
     * Creates additional files to new project.
     *
     * @param baseDir the base dir of the project.
     */
    private void createAdditionalFiles(@NotNull final VirtualFile baseDir) {
        try {

            // creates the native build script
            final VirtualFile nativeBuildFile = baseDir.createChildData(this, "build-native.xml");
            VfsUtil.saveText(nativeBuildFile, NATIVE_BUILD_SCRIPT);

            // creates example classes
            final VirtualFile mainDir = baseDir.createChildDirectory(this, "src")
                .createChildDirectory(this, "main");

            final VirtualFile assetsDir = mainDir.createChildDirectory(this, "assets");
            assetsDir.createChildDirectory(this, "Models");
            assetsDir.createChildDirectory(this, "Sounds");
            assetsDir.createChildDirectory(this, "Interface");
            assetsDir.createChildDirectory(this, "MatDefs");
            assetsDir.createChildDirectory(this, "Materials");
            assetsDir.createChildDirectory(this, "Shaders");

            final VirtualFile texturesDir = assetsDir.createChildDirectory(this, "Textures");
            final VirtualFile logoImageFile = texturesDir.createChildData(this, "jme-logo.png");
            logoImageFile.setBinaryContent(LOGO_IMAGE);

            final VirtualFile terrainAlphaDir = texturesDir.createChildDirectory(this, "TerrainAlpha");

            terrainAlphaDir.createChildData(this, "terrain-alpha-blend-1.png")
                .setBinaryContent(TERRAIN_ALPHA_1);
            terrainAlphaDir.createChildData(this, "terrain-alpha-blend-2.png")
                .setBinaryContent(TERRAIN_ALPHA_2);
            terrainAlphaDir.createChildData(this, "terrain-alpha-blend-3.png")
                .setBinaryContent(TERRAIN_ALPHA_3);

            final VirtualFile terrainDir = texturesDir.createChildDirectory(this, "Terrain");
            terrainDir.createChildData(this, "ground03.tga")
                .setBinaryContent(TERRAIN);

            final VirtualFile scenesDir = assetsDir.createChildDirectory(this, "Scenes");
            scenesDir.createChildData(this, "SimpleScene.j3s")
                .setBinaryContent(SIMPLE_SCENE);

            final VirtualFile exampleDir = mainDir.createChildDirectory(this, "java")
                .createChildDirectory(this, "com")
                .createChildDirectory(this, "jme")
                .createChildDirectory(this, "example");

            final VirtualFile gameApplicationFile = exampleDir.createChildData(this, "GameApplication.java");
            final VirtualFile starterFile = exampleDir.createChildData(this, "Starter.java");

            VfsUtil.saveText(gameApplicationFile, GAME_APPLICATION);
            VfsUtil.saveText(starterFile, STARTER);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
