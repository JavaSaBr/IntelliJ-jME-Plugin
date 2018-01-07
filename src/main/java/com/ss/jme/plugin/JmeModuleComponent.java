package com.ss.jme.plugin;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.ss.jme.plugin.jmb.JmbInstance;
import com.ss.jme.plugin.jmb.command.client.ClientCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

/**
 * The module level component of jME Plugin.
 *
 * @author JavaSaBr
 */
public class JmeModuleComponent implements ModuleComponent {

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

    private JmeModuleComponent(@NotNull final Module module) {
        this.module = module;
        this.rootManager = ModuleRootManager.getInstance(module);
        this.jmbInstance = new JmbInstance(module);
    }

    @Override
    public void initComponent() {
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
     * Get an asset folder of this module.
     *
     * @return the asset folder of this module.
     */
    public @Nullable Path getAssetFolder(@NotNull final VirtualFile file) {
        final String path = file.getPath();
        final VirtualFile[] sourceRoots = rootManager.getSourceRoots();
        return Arrays.stream(sourceRoots)
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

        final VirtualFile[] sourceRoots = rootManager.getSourceRoots();
        final Optional<VirtualFile> assetsFolder = Arrays.stream(sourceRoots)
                .filter(file -> file.getName().endsWith("assets"))
                .findFirst();

        if (assetsFolder.isPresent()) {
            return assetsFolder.get();
        }

        final Optional<VirtualFile> resourcesFolder = Arrays.stream(sourceRoots)
                .filter(file -> file.getName().endsWith("resources"))
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
    public void projectClosed() {

    }

    @Override
    public void disposeComponent() {

    }
}
