package com.ss.jme.plugin;

import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
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
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    public static final String FOLDER_ASSETS = "assets";

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

    private JmeModuleComponent(@NotNull Module module) {
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
    public void buildFinished(@NotNull Project project, @NotNull UUID sessionId, boolean isAutomake) {
        jmbInstance.sendCommandIfRunning(new LoadLocalClassesClientCommand(getCompileOutput()));
    }

    /**
     * Notifies about the module's project was resolved.
     */
    public void onProjectResolved() {
        jmbInstance.sendCommandIfRunning(new LoadLocalLibrariesClientCommand(getLibraries()));
    }

    /**
     * Gets all libraries of this module.
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
                .collect(ArrayCollectors.toArray(Path.class));
    }

    /**
     * Prepares the library path.
     *
     * @param path the library path
     * @return the prepared library path
     */
    private @NotNull String prepareLibraryPath(@NotNull String path) {
        if (path.endsWith("!/")) {
            return path.substring(0, path.length() - 2);
        } else if (path.endsWith("!\\")) {
            return path.substring(0, path.length() - 2);
        } else {
            return path;
        }
    }

    /**
     * Gets an asset folder of this module.
     *
     * @return the asset folder of this module.
     */
    public @Nullable Path getAssetFolder() {
        return findAssetsFolder()
                .map(VirtualFile::getPath)
                .map(Paths::get)
                .orElse(null);
    }

    /**
     * Gets the path to compilation output.
     *
     * @return the path to compilation output.
     */
    public @Nullable Path getCompileOutput() {
        return Optional.ofNullable(CompilerModuleExtension.getInstance(module))
                .map(CompilerModuleExtension::getCompilerOutputPath)
                .map(VirtualFile::getParent)
                .map(VirtualFile::getPath)
                .map(Paths::get)
                .orElse(null);
    }

    /**
     * Gets an asset folder of this module.
     *
     * @return the asset folder of this module.
     */
    public @Nullable Path getAssetFolder(@NotNull VirtualFile file) {
        String path = file.getPath();
        return rootManager.getSourceRoots(RESOURCE).stream()
                .filter(rootFile -> path.startsWith(rootFile.getPath()))
                .findFirst()
                .map(VirtualFile::getPath)
                .map(Paths::get)
                .orElse(null);
    }

    /**
     * Finds the assets folder of the module.
     *
     * @return the assets folder or null.
     */
    private @NotNull Optional<VirtualFile> findAssetsFolder() {

        Optional<VirtualFile> assetsFolder = rootManager.getSourceRoots(RESOURCE).stream()
                .filter(file -> file.getName().endsWith(FOLDER_ASSETS))
                .findFirst();

        if (assetsFolder.isPresent()) {
            return assetsFolder;
        }

        return rootManager.getSourceRoots(RESOURCE)
                .stream()
                .findFirst();
    }

    /**
     * Sends the command to jMB.
     *
     * @param command the command.
     */
    public void sendCommand(@NotNull ClientCommand command) {
        jmbInstance.sendCommand(command, module.getProject());
    }

    @Override
    public void disposeComponent() {
        notificationListener.dispose();
    }
}
