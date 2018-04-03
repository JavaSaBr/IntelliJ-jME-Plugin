package com.ss.jme.plugin.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.ss.jme.plugin.JmeMessagesBundle;
import com.ss.jme.plugin.JmePluginComponent;
import com.ss.jme.plugin.JmePluginState;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * The utility class.
 *
 * @author JavaSaBr
 */
public class JmePluginUtils {

    @NotNull
    private static final Logger LOG = Logger.getInstance("#com.ss.jme.plugin.util.JmePluginUtils");

    /**
     * Get or create folders by the names start from the parent folder.
     *
     * @param parent    the parent folder.
     * @param requester the requester.
     * @param names     the folder's names.
     * @return the created folder.
     */
    public static @NotNull VirtualFile getOrCreateFolders(
            @NotNull final VirtualFile parent,
            @NotNull final Object requester,
            @NotNull final String... names
    ) {

        final String name = names[0];

        VirtualFile resultFolder = parent.findChild(name);

        if (resultFolder == null) {
            resultFolder = Utils.get(() -> parent.createChildDirectory(requester, name));
        }

        if (names.length < 2) {
            return resultFolder;
        }

        return getOrCreateFolders(resultFolder, requester, Arrays.copyOfRange(names, 1, names.length));
    }

    /**
     * Get or create a file by the names start from the parent folder.
     *
     * @param parent    the parent folder.
     * @param requester the requester.
     * @param name      the file's names
     * @return the created file.
     */
    public static @NotNull
    VirtualFile getOrCreateFile(
            @NotNull final VirtualFile parent,
            @NotNull final Object requester,
            @NotNull final String name
    ) {

        VirtualFile resultFile = parent.findChild(name);

        if (resultFile == null) {
            resultFile = Utils.get(() -> parent.createChildData(requester, name));
        }

        return resultFile;
    }

    /**
     * Get the current path to jMB.
     *
     * @return the current path to jMB or null.
     */
    public static @Nullable Path getPathToJmb() {

        final JmePluginComponent pluginComponent = JmePluginComponent.getInstance();
        final JmePluginState state = pluginComponent.getState();
        final String jmbPath = state.getJmbPath();

        if (StringUtils.isEmpty(jmbPath)) {
            return null;
        }

        final Path path = Paths.get(jmbPath);
        if (!Files.exists(path)) {
            return null;
        }

        return path;
    }

    /**
     * Check jMB by the path.
     *
     * @param path the path to jMB.
     * @return true if we can work with this jMB.
     */
    public static boolean checkJmb(@NotNull final Path path) {

        final ProcessBuilder builder;

        if ("jar".equals(FileUtils.getExtension(path))) {
            final Path folder = path.getParent();
            builder = new ProcessBuilder("java", "-jar", path.toString());
            builder.directory(folder.toFile());
        } else {
            builder = new ProcessBuilder(path.toString());
        }

        builder.environment()
                .put("Server.api.version", String.valueOf(JmeConstants.JMB_API_VERSION));

        final Process process;
        try {
            process = builder.start();
        } catch (final IOException e) {
            LOG.warn(e);
            SwingUtilities.invokeLater(() -> {
                final String errorMessage = JmeMessagesBundle.message("jme.instance.error.cantExecute.message");
                final String resultMessage = errorMessage.replace("%path%", path.toString());
                final String title = JmeMessagesBundle.message("jme.instance.error.cantExecute.title");
                Messages.showWarningDialog(resultMessage, title);
            });
            return false;
        }

        boolean finished = false;
        try {
            finished = process.waitFor(2, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            LOG.warn(e);
        }

        if (!finished) {
            SwingUtilities.invokeLater(() -> {
                final String message = JmeMessagesBundle.message("jme.instance.error.doesNotSupport.messageByTimeout", path.toString());
                final String title = JmeMessagesBundle.message("jme.instance.error.doesNotSupport.title");
                Messages.showWarningDialog(message, title);
            });
            process.destroy();
            return false;
        }

        final int code = process.exitValue();
        if (code != 100) {
            SwingUtilities.invokeLater(() -> {
                final String message = JmeMessagesBundle.message("jme.instance.error.doesNotSupport.message", path.toString());
                final String title = JmeMessagesBundle.message("jme.instance.error.doesNotSupport.title");
                Messages.showWarningDialog(message, title);
            });
            return false;
        }

        return true;
    }
}
