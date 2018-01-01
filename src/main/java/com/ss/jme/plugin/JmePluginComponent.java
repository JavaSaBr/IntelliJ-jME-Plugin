package com.ss.jme.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main class of this plugin.
 *
 * @author JavaSaBr
 */
@State(name = "JmePluginComponent", storages = @Storage(id = "JmePluginComponent", file = StoragePathMacros.APP_CONFIG +
        "/jme_plugin.xml", scheme = StorageScheme.DIRECTORY_BASED))
public class JmePluginComponent implements ApplicationComponent, PersistentStateComponent<JmePluginState> {

    public static @NotNull JmePluginComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(JmePluginComponent.class);
    }

    @NotNull
    private JmePluginState state;

    public JmePluginComponent() {
        this.state = new JmePluginState();
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @Override
    public @NotNull JmePluginState getState() {
        return state;
    }

    @Override
    public void loadState(@Nullable final JmePluginState state) {
        this.state.copyOf(state);
    }
}
