package com.ss.jme.plugin.framework.support;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.ss.jme.plugin.JmeMessagesBundle;
import com.ss.jme.plugin.ui.JmeIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * The class to describe jMonkeyEngine framework in Gradle project creation wizard.
 *
 * @author JavaSaBr
 */
public class JmeFrameworkTypeEx extends FrameworkTypeEx {

    @NotNull
    private final JmeFrameworkSupportProvider provider;

    public JmeFrameworkTypeEx(@NotNull String id, @NotNull JmeFrameworkSupportProvider provider) {
        super(id);
        this.provider = provider;
    }

    @Override
    public @NotNull FrameworkSupportInModuleProvider createProvider() {
        return provider;
    }

    @Override
    public @NotNull String getPresentableName() {
        return JmeMessagesBundle.message("jme.framework.type.presentableName");
    }

    @Override
    public @NotNull Icon getIcon() {
        return JmeIcons.JME_16;
    }
}
