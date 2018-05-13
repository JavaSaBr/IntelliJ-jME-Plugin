package com.ss.jme.plugin.framework.support;

import static com.ss.jme.plugin.util.JmePluginUtils.getOrCreateFile;
import static com.ss.jme.plugin.util.JmePluginUtils.getOrCreateFolders;
import com.intellij.framework.FrameworkTypeEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.ss.rlib.common.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.frameworkSupport.BuildScriptDataBuilder;
import org.jetbrains.plugins.gradle.frameworkSupport.GradleFrameworkSupportProvider;

import java.io.IOException;

/**
 * The provider to create a jMonkeyEngine gradle build file with some additional files.
 *
 * @author JavaSaBr
 */
public class JmbFrameworkSupportProvider extends GradleFrameworkSupportProvider {

    private static final String MESSAGES_PATH = "com/ss/jme/plugin/plugin/template/messages.properties";
    private static final String PLUGIN_CLASS_PATH = "com/ss/jme/plugin/plugin/template/ExamplePlugin.java";
    private static final String PLUGIN_MESSAGES_PATH = "com/ss/jme/plugin/plugin/template/PluginMessages.java";
    private static final String PLUGIN_CLASS;
    private static final String PLUGIN_MESSAGES_CLASS;
    private static final String MESSAGES;

    static {
        final ClassLoader classLoader = JmbFrameworkSupportProvider.class.getClassLoader();
        PLUGIN_CLASS = FileUtils.read(classLoader.getResourceAsStream(PLUGIN_CLASS_PATH));
        PLUGIN_MESSAGES_CLASS = FileUtils.read(classLoader.getResourceAsStream(PLUGIN_MESSAGES_PATH));
        MESSAGES = FileUtils.read(classLoader.getResourceAsStream(MESSAGES_PATH));
    }

    public JmbFrameworkSupportProvider() {
    }

    @Override
    public @NotNull FrameworkTypeEx getFrameworkType() {
        return new JmbFrameworkTypeEx("jMonkeyBuilder", this);
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
                .addImport("import java.nio.file.Files")
                .addImport("import java.nio.file.Paths")

                .addRepositoriesDefinition("jcenter()")
                .addRepositoriesDefinition("mavenCentral()")
                .addRepositoriesDefinition("maven { url \"https://jitpack.io\" }")
                .addRepositoriesDefinition("maven { url \"https://dl.bintray.com/javasabr/maven\" }")

                .addPluginDefinition("apply plugin: 'maven'")
                .addPluginDefinition("apply plugin: 'java'")
                .addPluginDefinition("apply plugin: 'application'")

                .addPropertyDefinition("ext.artifactId = 'jmb-plugin-example'")
                .addPropertyDefinition("mainClassName = \"com.ss.editor.DevelopPluginStarter\"")
                .addPropertyDefinition("sourceCompatibility = 1.10")
                .addPropertyDefinition("targetCompatibility = 1.10")
                .addPropertyDefinition("configurations {\n" +
                        "    pluginDependencies\n" +
                        "}")

                .addDependencyNotation("compile 'com.spaceshift:jmonkeybuilder:1.7.3-Final'")

                .addOther("task cleanPluginFolders(type: Delete) {\n" +
                        "    doFirst {\n" +
                        "\n" +
                        "        def projectFolder = Paths.get(\"$buildDir\").getParent()\n" +
                        "        def embeddedPluginsFolder = projectFolder.resolve(\"embedded-plugins\")\n" +
                        "        def deployPlugin = projectFolder.resolve(\"deploy-plugin\")\n" +
                        "\n" +
                        "        if (Files.exists(embeddedPluginsFolder)) {\n" +
                        "            GFileUtils.deleteDirectory(embeddedPluginsFolder.toFile())\n" +
                        "        }\n" +
                        "\n" +
                        "        if (Files.exists(deployPlugin)) {\n" +
                        "            GFileUtils.deleteDirectory(deployPlugin.toFile())\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
                .addOther("task createPluginFolders(dependsOn: 'cleanPluginFolders') {\n" +
                        "    doFirst {\n" +
                        "\n" +
                        "        def projectFolder = Paths.get(\"$buildDir\").getParent()\n" +
                        "        def embeddedPluginsFolder = projectFolder.resolve(\"embedded-plugins\")\n" +
                        "\n" +
                        "        Files.createDirectory(embeddedPluginsFolder)\n" + "\n" +
                        "        def pluginFolder = embeddedPluginsFolder.resolve(String.valueOf(artifactId))\n" +
                        "\n" +
                        "        Files.createDirectory(pluginFolder)\n" + "\n" +
                        "        def configuration = configurations.pluginDependencies\n" +
                        "        def dependencies = configuration.dependencies\n" +
                        "\n" +
                        "        if (dependencies.isEmpty()) {\n" +
                        "            return\n" +
                        "        }\n" +
                        "\n" +
                        "        def libsFolder = pluginFolder.resolve(\"libs\")\n" +
                        "\n" +
                        "        Files.createDirectory(libsFolder)\n" +
                        "    }\n" +
                        "}")
                .addOther("task copyPluginDependencies(type: Copy, dependsOn: ['install', 'createPluginFolders']) {\n" +
                        "\n" +
                        "    def configuration = configurations.pluginDependencies\n" +
                        "    def dependencies = configuration.dependencies\n" +
                        "\n" +
                        "    if (dependencies.isEmpty()) {\n" +
                        "        return\n" +
                        "    }\n" +
                        "\n" +
                        "    def projectFolder = Paths.get(\"$buildDir\").getParent()\n" +
                        "    def embeddedPluginsFolder = projectFolder.resolve(\"embedded-plugins\")\n" +
                        "    def pluginFolder = embeddedPluginsFolder.resolve(String.valueOf(artifactId))\n" +
                        "    def libsFolder = pluginFolder.resolve(\"libs\")\n" + "\n" +
                        "    into libsFolder.toString()\n" + "    from configuration\n" + "}")
                .addOther("task preparePlugin(type: Copy, dependsOn: 'copyPluginDependencies') {\n" +
                        "\n" +
                        "    def projectFolder = Paths.get(\"$buildDir\").getParent()\n" +
                        "    def embeddedPluginsFolder = projectFolder.resolve(\"embedded-plugins\")\n" +
                        "    def pluginFolder = embeddedPluginsFolder.resolve(String.valueOf(artifactId))\n" +
                        "\n" +
                        "    from jar\n" +
                        "    into pluginFolder.toString()\n" +
                        "}")
                .addOther("task deployPlugin(type: Zip, dependsOn: 'preparePlugin') {\n" +
                        "    doFirst {\n" +
                        "\n" +
                        "        def projectFolder = Paths.get(\"$buildDir\").getParent()\n" +
                        "        def deployPluginFolder = projectFolder.resolve(\"deploy-plugin\")\n" +
                        "\n" +
                        "        if (!Files.exists(deployPluginFolder)) {\n" +
                        "            Files.createDirectory(deployPluginFolder)\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    def projectFolder = Paths.get(\"$buildDir\").getParent()\n" +
                        "    def embeddedPluginsFolder = projectFolder.resolve(\"embedded-plugins\")\n" +
                        "    def pluginFolder = embeddedPluginsFolder.resolve(String.valueOf(artifactId))\n" +
                        "    def deployPluginFolder = projectFolder.resolve(\"deploy-plugin\")\n" +
                        "\n" +
                        "    from pluginFolder.toString()\n" +
                        "    destinationDir deployPluginFolder.toFile()\n" +
                        "}")
                .addOther("task wrapper(type: Wrapper) {\n" +
                        "    gradleVersion = '4.6'\n" +
                        "}")
                .addOther("run.dependsOn {\n" +
                        "    preparePlugin\n" +
                        "}\n" +
                        "run.jvmArgs(Arrays.asList(\"-Xdebug\", \"-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005\"))");

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

            VirtualFile mainDir = getOrCreateFolders(baseDir,this,"src", "main");

            VirtualFile java = getOrCreateFolders(mainDir, this, "java");
            VirtualFile resources = getOrCreateFolders(mainDir, this, "resources");
            VirtualFile messagesFolder = getOrCreateFolders(resources, this,
                    "plugin", "example", "messages");

            VirtualFile messagesFile = getOrCreateFile(messagesFolder, this,
                    "messages.properties");
            VirtualFile rootSourceFolder = getOrCreateFolders(java, this,
                    "com", "ss", "editor", "plugin", "example");

            VfsUtil.saveText(messagesFile, MESSAGES);

            VirtualFile pluginClass = getOrCreateFile(rootSourceFolder, this,
                    "ExamplePlugin.java");

            VfsUtil.saveText(pluginClass, PLUGIN_CLASS);

            VirtualFile pluginMessagesClass = getOrCreateFile(rootSourceFolder, this,
                    "PluginMessages.java");

            VfsUtil.saveText(pluginMessagesClass, PLUGIN_MESSAGES_CLASS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
