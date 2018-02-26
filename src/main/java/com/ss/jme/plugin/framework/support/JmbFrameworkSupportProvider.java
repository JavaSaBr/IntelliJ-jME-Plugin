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
    public void addSupport(@NotNull final ProjectId projectId, @NotNull final Module module,
                           @NotNull final ModifiableRootModel rootModel,
                           @NotNull final ModifiableModelsProvider modifiableModelsProvider,
                           @NotNull final BuildScriptDataBuilder buildScriptData) {

        buildScriptData

                .addImport("import java.nio.file.Files")
                .addImport("import java.nio.file.Paths")

                .addRepositoriesDefinition("jcenter()")
                .addRepositoriesDefinition("mavenCentral()")
                .addRepositoriesDefinition("maven { url 'https://jitpack.io' }")

                .addPluginDefinition("apply plugin: 'maven'")
                .addPluginDefinition("apply plugin: 'java'")
                .addPluginDefinition("apply plugin: 'application'")

                .addPropertyDefinition("ext.artifactId = 'jmb-plugin-example'")
                .addPropertyDefinition("mainClassName = \"com.ss.editor.DevelopPluginStarter\"")
                .addPropertyDefinition("sourceCompatibility = 1.8")
                .addPropertyDefinition("targetCompatibility = 1.8")
                .addPropertyDefinition("configurations {\n" +
                        "    pluginDependencies\n" +
                        "}")

                .addDependencyNotation("compile 'com.github.JavaSaBr:jmonkeybuilder:develop-SNAPSHOT'")

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
                .addOther("tasks.run.dependsOn('preparePlugin')")
                .addOther("task wrapper(type: Wrapper) {\n" +
                        "    gradleVersion = '4.5.1'\n" +
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

            final VirtualFile mainDir = baseDir.createChildDirectory(this, "src")
                    .createChildDirectory(this, "main");

            final VirtualFile java = mainDir.createChildDirectory(this, "java");
            final VirtualFile resources = mainDir.createChildDirectory(this, "resources");

            final VirtualFile messagesFolder = resources.createChildDirectory(this, "plugin")
                    .createChildDirectory(this, "example")
                    .createChildDirectory(this, "messages");

            final VirtualFile messagesFile = messagesFolder.createChildData(this, "messages.properties");

            final VirtualFile rootSourceFolder = java.createChildDirectory(this, "com")
                    .createChildDirectory(this, "ss")
                    .createChildDirectory(this, "editor")
                    .createChildDirectory(this, "plugin")
                    .createChildDirectory(this, "example");

            VfsUtil.saveText(messagesFile, MESSAGES);

            final VirtualFile pluginClass = rootSourceFolder.createChildData(this, "ExamplePlugin.java");
            VfsUtil.saveText(pluginClass, PLUGIN_CLASS);

            final VirtualFile pluginMessagesClass = rootSourceFolder.createChildData(this, "PluginMessages.java");
            VfsUtil.saveText(pluginMessagesClass, PLUGIN_MESSAGES_CLASS);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
