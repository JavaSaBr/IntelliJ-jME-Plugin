package com.ss.jme.plugin.ui.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.ss.jme.plugin.JmePluginComponent;
import com.ss.jme.plugin.JmePluginState;
import com.ss.rlib.util.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * The page to provide some settings of integration with jMonkeyEngine.
 *
 * @author JavaSaBr
 */
public class JmeExternalSettingsPage implements Configurable, Configurable.NoScroll {

    /**
     * The settings panel.
     */
    @Nullable
    private JmeConfigurablePanel settingsPanel;

    private JmeExternalSettingsPage() {
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsPanel = new JmeConfigurablePanel();
        return settingsPanel.panel;
    }

    @Override
    public void apply() {
        getSettingsPanel().ifPresent(JmeConfigurablePanel::apply);
    }

    @Override
    public boolean isModified() {
        return getSettingsPanel().map(JmeConfigurablePanel::isModified)
                .orElse(false);
    }

    @Override
    public void reset() {
        getSettingsPanel().ifPresent(JmeConfigurablePanel::reset);
    }

    @Override
    public void disposeUIResources() {
        settingsPanel = null;
    }

    private @NotNull Optional<JmeConfigurablePanel> getSettingsPanel() {
        return Optional.ofNullable(settingsPanel);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "jMonkeyEngine";
    }

    public static @NotNull FileChooserDescriptor createSceneBuilderDescriptor() {
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
        descriptor.setTitle("jMonkeyBuilder Configuration");
        descriptor.setDescription("Select path to jMonkeyBuilder executable");
        return descriptor;
    }

    public static class JmeConfigurablePanel {

        @NotNull
        private final TextFieldWithBrowseButton pathField;

        @NotNull
        private final JPanel panel;

        public JmeConfigurablePanel() {
            this.panel = new JBPanel<>(new BorderLayout());
            this.pathField = new TextFieldWithBrowseButton();

            final FileChooserDescriptor descriptor = createSceneBuilderDescriptor();
            pathField.addBrowseFolderListener(descriptor.getTitle(), descriptor.getDescription(), null, descriptor);

            final JBLabel label = new JBLabel("Path to jMonkeyBuilder:");
            label.setHorizontalAlignment(SwingConstants.LEFT);

            final JPanel wrapper = new JBPanel<>(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.LINE_START;
            constraints.insets = JBUI.insets(5, 5, 0, 5);
            constraints.gridx = 0;
            constraints.gridy = 0;

            wrapper.add(label, constraints);

            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.LINE_START;
            constraints.insets = JBUI.insets(5, 0, 0, 5);
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.weightx = 0.5;
            constraints.gridwidth = 2;

            wrapper.add(pathField, constraints);
            panel.add(wrapper, BorderLayout.PAGE_START);
        }

        private void reset() {

            final String jmbPath = JmePluginComponent.getInstance()
                    .getState()
                    .getJmbPath();

            if (StringUtils.isNotEmpty(jmbPath)) {
                pathField.setText(FileUtil.toSystemDependentName(jmbPath));
            } else {
                pathField.setText(null);
            }
        }

        private void apply() {
            final JmePluginComponent component = JmePluginComponent.getInstance();
            final JmePluginState state = component.getState();
            state.setJmbPath(FileUtil.toSystemIndependentName(pathField.getText().trim()));
        }

        private boolean isModified() {

            final String jmbPath = JmePluginComponent.getInstance()
                    .getState()
                    .getJmbPath();

            return !Comparing.strEqual(FileUtil.toSystemIndependentName(pathField.getText().trim()), jmbPath.trim());
        }
    }
}
