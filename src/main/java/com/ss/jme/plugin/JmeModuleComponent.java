package com.ss.jme.plugin;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.ss.jme.plugin.jmb.JmbInstance;
import com.ss.jme.plugin.jmb.command.client.ClientCommand;
import com.ss.jme.plugin.jmb.command.client.LoadLocalClassesClientCommand;
import com.ss.jme.plugin.jmb.command.client.LoadLocalLibrariesClientCommand;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayCollectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * The module level component of jME Plugin.
 *
 * @author JavaSaBr
 */
public class JmeModuleComponent implements ModuleComponent, BuildManagerListener {

    /**
     * The module root manager.
     */
    @NotNull
    private final ModuleRootManager rootManager;

    /**
     * The module.
     */
    @NotNull
    private final Module module;

    /**
     * The instance of jMB.
     */
    @NotNull
    private final JmbInstance jmbInstance;

    /**
     * The notification listener.
     */
    @NotNull
    private final JmeExternalSystemTaskNotificationListener notificationListener;

    private JmeModuleComponent(@NotNull final Module module) {
        this.module = module;
        this.rootManager = ModuleRootManager.getInstance(module);
        this.jmbInstance = new JmbInstance(module);
        this.notificationListener = new JmeExternalSystemTaskNotificationListener(module);
    }

    @Override
    public void initComponent() {
        module.getMessageBus().connect()
                .subscribe(BuildManagerListener.TOPIC, this);
    }

    @Override
    public void buildFinished(@NotNull final Project project, @NotNull final UUID sessionId, final boolean isAutomake) {
        jmbInstance.sendCommandIfRunning(new LoadLocalClassesClientCommand(getCompileOutput()));
    }

    /**
     * Notify about the module's project was resolved.
     */
    public void onProjectResolved() {
        jmbInstance.sendCommandIfRunning(new LoadLocalLibrariesClientCommand(getLibraries()));
    }

    /**
     * Get all libraries of this module.
     *
     * @return the list of libraries.
     */
    public @NotNull Array<Path> getLibraries() {
        return Arrays.stream(rootManager.getOrderEntries())
                .filter(LibraryOrderEntry.class::isInstance)
                .map(LibraryOrderEntry.class::cast)
                .filter(entry -> entry.getScope() == DependencyScope.COMPILE)
                .flatMap(orderEntry -> Arrays.stream(orderEntry.getFiles(OrderRootType.CLASSES)))
                .map(VirtualFile::getPath)
                .map(this::prepareLibraryPath)
                .map(path -> Paths.get(path))
                .collect(ArrayCollectors.simple(Path.class));
    }

    /**
     * Prepare the library path.
     *
     * @param path the library path
     * @return the prepared library path
     */
    private @NotNull String prepareLibraryPath(@NotNull final String path) {
        if (path.endsWith("!/")) {
            return path.substring(0, path.length() - 2);
        } else if (path.endsWith("!\\")) {
            return path.substring(0, path.length() - 2);
        }
        return path;
    }

    /**
     * Get an asset folder of this module.
     *
     * @return the asset folder of this module.
     */
    public @Nullable Path getAssetFolder() {
        final VirtualFile assetsFolder = findAssetsFolder();
        if (assetsFolder == null) return null;
        return Paths.get(assetsFolder.getPath());
    }

    /**
     * Get the path to compilation output.
     *
     * @return the path to compilation output.
     */
    public @Nullable Path getCompileOutput() {
        final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
        if (extension == null) return null;
        final VirtualFile outputPath = extension.getCompilerOutputPath();
        if (outputPath == null) return null;
        return Paths.get(outputPath.getParent().getPath());
    }

    /**
     * Get an asset folder of this module.
     *
     * @return the asset folder of this module.
     */
    public @Nullable Path getAssetFolder(@NotNull final VirtualFile file) {
        final String path = file.getPath();
        return rootManager.getSourceRoots(JavaResourceRootType.RESOURCE).stream()
                .filter(rootFile -> path.startsWith(rootFile.getPath()))
                .findFirst().map(result -> Paths.get(result.getPath()))
                .orElse(null);
    }

    /**
     * Finds the assets folder of the module.
     *
     * @return the assets folder or null.
     */
    private @Nullable VirtualFile findAssetsFolder() {

        final Optional<VirtualFile> assetsFolder = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE).stream()
                .filter(file -> file.getName().endsWith("assets"))
                .findFirst();

        if (assetsFolder.isPresent()) {
            return assetsFolder.get();
        }

        final Optional<VirtualFile> resourcesFolder = rootManager.getSourceRoots(JavaResourceRootType.RESOURCE).stream()
                .findFirst();

        return resourcesFolder.orElse(null);
    }

    /**
     * Send the command to jMB.
     *
     * @param command the command.
     */
    public void sendCommand(@NotNull final ClientCommand command) {
        jmbInstance.sendCommand(command);
    }

    @Override
    public void disposeComponent() {
        notificationListener.dispose();
    }
}
