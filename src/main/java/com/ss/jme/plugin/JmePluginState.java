package com.ss.jme.plugin;

import com.intellij.util.xmlb.annotations.Property;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The main state of this plugin.
 *
 * @author JavaSaBr
 */
@Getter
@EqualsAndHashCode
public class JmePluginState {

    @NotNull
    private static final String DEFAULT_JMB_PATH = "";

    @NotNull
    @Property
    private String jmbPath;

    JmePluginState() {
        this.jmbPath = DEFAULT_JMB_PATH;
    }

    /**
     * Copies state from the instance.
     *
     * @param other the other state instance.
     */
    void copyOf(@Nullable JmePluginState other) {
        if (other == null) {
            this.jmbPath = DEFAULT_JMB_PATH;
        } else {
            this.jmbPath = other.jmbPath;
        }
    }

    /**
     * Sets the path to jMB.
     *
     * @param jmbPath the path to jMB.
     */
    public void setJmbPath(@Nullable String jmbPath) {
        this.jmbPath = jmbPath == null ? DEFAULT_JMB_PATH : jmbPath;
    }
}
