package com.ss.jme.plugin;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main state of this plugin.
 *
 * @author JavaSaBr
 */
public class JmePluginState {

    @NotNull
    private static final String DEFAULT_JMB_PATH = "";

    @NotNull
    @Property
    private String jmbPath;

    public JmePluginState() {
        this.jmbPath = DEFAULT_JMB_PATH;
    }

    public void copyOf(@Nullable final JmePluginState other) {
        if (other == null) {
            this.jmbPath = DEFAULT_JMB_PATH;
        } else {
            this.jmbPath = other.jmbPath;
        }
    }

    public @NotNull String getJmbPath() {
        return jmbPath;
    }

    public void setJmbPath(@Nullable final String jmbPath) {
        this.jmbPath = jmbPath == null? DEFAULT_JMB_PATH : jmbPath;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JmePluginState that = (JmePluginState) o;
        return getJmbPath().equals(that.getJmbPath());
    }

    @Override
    public int hashCode() {
        return getJmbPath().hashCode();
    }
}
