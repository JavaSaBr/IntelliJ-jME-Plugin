package com.ss.jme.plugin.framework.support;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author JavaSaBr
 */
public class JmeFrameworkTypeEx extends FrameworkTypeEx {

    @NotNull
    private final JmeFrameworkSupportProvider provider;

    public JmeFrameworkTypeEx(@NotNull final String id, @NotNull final JmeFrameworkSupportProvider provider) {
        super(id);
        this.provider = provider;
    }

    @Override
    public @NotNull FrameworkSupportInModuleProvider createProvider() {
        return provider;
    }


    @Override
    public @NotNull String getPresentableName() {
        return "jMonkeyEngine 3.1 Desktop";
    }

    @Override
    public @NotNull Icon getIcon() {
        return AllIcons.Nodes.Module;
    }
}
