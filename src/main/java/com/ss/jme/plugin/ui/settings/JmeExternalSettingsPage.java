package com.ss.jme.plugin.ui.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.ss.jme.plugin.JmeMessagesBundle;
import com.ss.jme.plugin.JmePluginComponent;
import com.ss.jme.plugin.JmePluginState;
import com.ss.jme.plugin.util.JmePluginUtils;
import com.ss.rlib.common.util.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * The page to provide some settings of integration with jMonkeyEngine.
 *
 * @author JavaSaBr
 */
public class JmeExternalSettingsPage implements Configurable, Configurable.NoScroll {

    /**
     * Creates a file chooser descriptor to select an executable file of jMB.
     *
     * @return the file chooser descriptor.
     */
    public static @NotNull FileChooserDescriptor createJmbDescriptor() {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        descriptor.setTitle(JmeMessagesBundle.message("jme.settings.pathToJmb.file.chooser.title"));
        descriptor.setDescription(JmeMessagesBundle.message("jme.settings.pathToJmb.file.chooser.description"));
        return descriptor;
    }

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

    /**
     * Gets the settings panel.
     *
     * @return the settings panel.
     */
    private @NotNull Optional<JmeConfigurablePanel> getSettingsPanel() {
        return Optional.ofNullable(settingsPanel);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return JmeMessagesBundle.message("jme.settings.displayName");
    }

    /**
     * The UI panel to show settings control.
     */
    public static class JmeConfigurablePanel {

        @NotNull
        private final TextFieldWithBrowseButton pathField;

        @NotNull
        private final JPanel panel;

        public JmeConfigurablePanel() {
            this.panel = new JBPanel<>(new BorderLayout());
            this.pathField = new TextFieldWithBrowseButton();

            FileChooserDescriptor descriptor = createJmbDescriptor();
            pathField.addBrowseFolderListener(descriptor.getTitle(), descriptor.getDescription(), null, descriptor);

            JBLabel label = new JBLabel(JmeMessagesBundle.message("jme.settings.label.pathToJmb"));
            label.setHorizontalAlignment(SwingConstants.LEFT);

            JPanel wrapper = new JBPanel<>(new GridBagLayout());

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

        /**
         * Resets settings.
         */
        private void reset() {

            String jmbPath = JmePluginComponent.getInstance()
                    .getState()
                    .getJmbPath();

            if (StringUtils.isNotEmpty(jmbPath)) {
                pathField.setText(FileUtil.toSystemDependentName(jmbPath));
            } else {
                pathField.setText(null);
            }
        }

        /**
         * Applies settings.
         */
        private void apply() {

            JmePluginComponent component = JmePluginComponent.getInstance();
            JmePluginState state = component.getState();

            String jmbPath = FileUtil.toSystemIndependentName(pathField.getText().trim());

            if (StringUtils.isEmpty(jmbPath)) {
                state.setJmbPath("");
                return;
            }

            Path path = Paths.get(jmbPath);
            if (!Files.exists(path)) {
                String errorMessage = JmeMessagesBundle.message("jme.settings.pathToJmb.fileNotExists.message");
                String resultMessage = errorMessage.replace("%path%", path.toString());
                String title = JmeMessagesBundle.message("jme.settings.pathToJmb.fileNotExists.title");
                Messages.showWarningDialog(resultMessage, title);
                return;
            }

            if (!JmePluginUtils.checkJmb(path)) {
                return;
            }

            state.setJmbPath(jmbPath);
        }

        /**
         * Checks modifications of settings.
         *
         * @return true if the settings were changed.
         */
        private boolean isModified() {

            String jmbPath = JmePluginComponent.getInstance()
                    .getState()
                    .getJmbPath();

            return !Comparing.strEqual(FileUtil.toSystemIndependentName(pathField.getText().trim()), jmbPath.trim());
        }
    }
}
