package com.ss.jme.plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main class of this plugin.
 *
 * @author JavaSaBr
 */
@State(name = "JmePluginComponent", storages = @Storage(value = "jme.plugin.xml"))
public class JmePluginComponent implements PersistentStateComponent<JmePluginState> {

    public static @NotNull JmePluginComponent getInstance() {
        return ServiceManager.getService(JmePluginComponent.class);
    }

    @NotNull
    private final JmePluginState state;

    public JmePluginComponent() {
        this.state = new JmePluginState();
    }

    @Override
    public @NotNull JmePluginState getState() {
        return state;
    }

    @Override
    public void loadState(@Nullable JmePluginState state) {
        this.state.copyOf(state);
    }
}
