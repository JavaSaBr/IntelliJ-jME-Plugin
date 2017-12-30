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
    private static final String GAME_APPLICATION_PATH = "com/ss/jme/plugin/project/template/GameApplication.java";
    private static final String STARTER_PATH = "com/ss/jme/plugin/project/template/Starter.java";

    @NotNull
    private static final String NATIVE_BUILD_SCRIPT;
    @NotNull
    private static final String GAME_APPLICATION;

    @NotNull
    private static final String STARTER;

    @NotNull
    private static final byte[] LOGO_IMAGE;

    static {
        try {
            final ClassLoader classLoader = JmeFrameworkSupportProvider.class.getClassLoader();
            NATIVE_BUILD_SCRIPT = FileUtils.read(classLoader.getResourceAsStream(NATIVE_BUILD_SCRIPT_PATH));
            GAME_APPLICATION = FileUtils.read(classLoader.getResourceAsStream(GAME_APPLICATION_PATH));
            STARTER = FileUtils.read(classLoader.getResourceAsStream(STARTER_PATH));
            LOGO_IMAGE = IOUtils.toByteArray(classLoader.getResourceAsStream(LOGO_IMAGE_PATH));
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

        buildScriptData.addBuildscriptRepositoriesDefinition("mavenCentral()")
                .addBuildscriptDependencyNotation("classpath 'org.junit.platform:junit-platform-gradle-plugin:1.0.0'")
                .addPluginDefinition("apply plugin: 'maven'")
                .addPluginDefinition("apply plugin: 'idea'")
                .addPluginDefinition("apply plugin: 'java'")
                .addPluginDefinition("apply plugin: 'org.junit.platform.gradle.plugin'")
                .addPropertyDefinition("sourceCompatibility = 1.8")
                .addPropertyDefinition("targetCompatibility = 1.8")
                .addPropertyDefinition("// replace resources folder to asset folder\n" +
                        "sourceSets {\n" +
                        "    main {\n" +
                        "        resources {\n" +
                        "            srcDirs= [\"src/main/assets\"]\n" +
                        "        }\n" +
                        "    }\n" +
                        "    test {\n" +
                        "        resources {\n" +
                        "            srcDirs= [\"src/test/assets\"]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n\n" +
                        "// java version of tests\n" +
                        "compileTestJava {\n" +
                        "    sourceCompatibility = 1.8\n" +
                        "    targetCompatibility = 1.8\n" +
                        "    options.compilerArgs += '-parameters'\n" +
                        "}\n\n" +
                        "// additional properties for native build \n" +
                        "ext.applicationTitle = \"jME exapmle\"\n" +
                        "ext.applicationVendor = \"No vendor\"\n" +
                        "ext.applicationMainClass = \"com.jme.example.Starter\"\n\n" +
                        "// configuring junit tests\n" +
                        "ext.junitPlatformVersion = '1.0.0'\n" +
                        "ext.junitJupiterVersion = '5.0.0'\n" +
                        "ext.log4jVersion = '2.6.2'\n\n" +
                        "// configuring junit platform\n" +
                        "junitPlatform {\n" +
                        "    logManager 'org.apache.logging.log4j.jul.LogManager'\n" +
                        "}\n\n" +
                        "// define version of jME\n" +
                        "def jme3 = [v: '3.1.0-stable', g: 'org.jmonkeyengine']\n")
                .addRepositoriesDefinition("maven { url \"http://dl.bintray.com/jmonkeyengine/org.jmonkeyengine\" }")
                .addDependencyNotation("compile \"${jme3.g}:jme3-core:${jme3.v}\"")
                .addDependencyNotation("runtime \"${jme3.g}:jme3-lwjgl3:${jme3.v}\"")
                .addDependencyNotation("runtime \"${jme3.g}:jme3-desktop:${jme3.v}\"")
                .addDependencyNotation("testCompile \"${jme3.g}:jme3-core:${jme3.v}\"")
                .addDependencyNotation("testRuntime \"${jme3.g}:jme3-lwjgl3:${jme3.v}\"")
                .addDependencyNotation("testRuntime \"${jme3.g}:jme3-desktop:${jme3.v}\"")

                .addDependencyNotation("testCompile \"org.junit.platform:junit-platform-commons:$junitPlatformVersion\"")
                .addDependencyNotation("testRuntime \"org.junit.platform:junit-platform-engine:$junitPlatformVersion\"")
                .addDependencyNotation("testCompile \"org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion\"")
                .addDependencyNotation("testRuntime \"org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion\"")
                .addDependencyNotation("testRuntime \"org.apache.logging.log4j:log4j-core:$log4jVersion\"")
                .addDependencyNotation("testRuntime \"org.apache.logging.log4j:log4j-jul:$log4jVersion\"")
                .addDependencyNotation("// Only needed to run tests in an (IntelliJ) IDE(A) that bundles an older version\n" +
                        "    testCompile \"org.junit.platform:junit-platform-launcher:$junitPlatformVersion\"")
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
            final VirtualFile srcDir = baseDir.createChildDirectory(this, "src");
            final VirtualFile mainDir = srcDir.createChildDirectory(this, "main");
            final VirtualFile assetsDir = mainDir.createChildDirectory(this, "assets");
            final VirtualFile texturesDir = assetsDir.createChildDirectory(this, "textures");

            final VirtualFile logoImageFile = texturesDir.createChildData(this, "jme-logo.png");
            logoImageFile.setBinaryContent(LOGO_IMAGE);

            final VirtualFile javaDir = mainDir.createChildDirectory(this, "java");
            final VirtualFile comDir = javaDir.createChildDirectory(this, "com");
            final VirtualFile jmeDir = comDir.createChildDirectory(this, "jme");
            final VirtualFile exampleDir = jmeDir.createChildDirectory(this, "example");
            final VirtualFile gameApplicationFile = exampleDir.createChildData(this, "GameApplication.java");
            final VirtualFile starterFile = exampleDir.createChildData(this, "Starter.java");

            VfsUtil.saveText(gameApplicationFile, GAME_APPLICATION);
            VfsUtil.saveText(starterFile, STARTER);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
