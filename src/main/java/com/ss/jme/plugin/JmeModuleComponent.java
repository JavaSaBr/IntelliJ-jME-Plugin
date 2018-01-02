package com.ss.jme.plugin;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private JmeModuleComponent(@NotNull final Module module) {
        this.module = module;
        this.rootManager = ModuleRootManager.getInstance(module);
    }

    @Override
    public void initComponent() {
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

    @Override
    public void projectClosed() {

    }

    @Override
    public void disposeComponent() {

    }
}
