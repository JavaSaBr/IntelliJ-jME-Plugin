package com.ss.jme.plugin.framework.support;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.ss.jme.plugin.JmeMessagesBundle;
import com.ss.jme.plugin.ui.JmeIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * The class to describe jMonkeyBuilder framework in Gradle project creation wizard.
 *
 * @author JavaSaBr
 */
public class JmbFrameworkTypeEx extends FrameworkTypeEx {

    @NotNull
    private final JmbFrameworkSupportProvider provider;

    public JmbFrameworkTypeEx(@NotNull String id, @NotNull JmbFrameworkSupportProvider provider) {
        super(id);
        this.provider = provider;
    }

    @Override
    public @NotNull FrameworkSupportInModuleProvider createProvider() {
        return provider;
    }

    @Override
    public @NotNull String getPresentableName() {
        return JmeMessagesBundle.message("jmb.framework.type.presentableName");
    }

    @Override
    public @NotNull Icon getIcon() {
        return JmeIcons.JMB_16;
    }
}
