package com.ss.jme.plugin.jmb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.ss.jme.plugin.JmeMessagesBundle;
import com.ss.jme.plugin.JmeModuleComponent;
import com.ss.jme.plugin.jmb.command.client.ClientCommand;
import com.ss.jme.plugin.jmb.command.client.InitClasspathClientCommand;
import com.ss.jme.plugin.jmb.command.server.EmptyServerCommand;
import com.ss.jme.plugin.util.JmePluginUtils;
import com.ss.rlib.concurrent.util.ConcurrentUtils;
import com.ss.rlib.concurrent.util.ThreadUtils;
import com.ss.rlib.network.NetworkConfig;
import com.ss.rlib.network.NetworkFactory;
import com.ss.rlib.network.client.ClientNetwork;
import com.ss.rlib.network.client.ConnectHandler;
import com.ss.rlib.network.client.server.Server;
import com.ss.rlib.network.packet.ReadablePacketRegistry;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The class to present an instance of jMB.
 *
 * @author JavaSaBr
 */
public class JmbInstance extends Thread {

    @NotNull
    private static final Logger LOG = Logger.getInstance("#com.ss.jme.plugin.jmb.JmbInstance");

    @NotNull
    private static final ReadablePacketRegistry PACKET_REGISTRY = ReadablePacketRegistry.of(EmptyServerCommand.class);

    @NotNull
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @NotNull
    private static final NetworkConfig NETWORK_CONFIG = new NetworkConfig() {

        @Override
        public int getReadBufferSize() {
            return Short.MAX_VALUE * 2;
        }

        @Override
        public int getWriteBufferSize() {
            return Short.MAX_VALUE * 2;
        }
    };

    /**
     * The module.
     */
    @NotNull
    private final Module module;

    /**
     * The notificator.
     */
    @NotNull
    private final Object notificator;

    /**
     * The client network.
     */
    @NotNull
    private final ClientNetwork clientNetwork;

    /**
     * The process.
     */
    @Nullable
    private volatile Process process;

    /**
     * The server of jMB.
     */
    @Nullable
    private volatile Server server;

    /**
     * The last used path to jMB.
     */
    @Nullable
    private volatile Path lastJmbPath;

    /**
     * The flag of reading to work with jMB.
     */
    private volatile boolean ready;

    /**
     * The flag about that last attempt was failed.
     */
    private volatile boolean wasFailed;

    public JmbInstance(@NotNull final Module module) {
        this.module = module;
        this.notificator = new Object();
        this.clientNetwork = NetworkFactory.newDefaultAsyncClientNetwork(NETWORK_CONFIG, PACKET_REGISTRY, ConnectHandler.newDefault());
        start();
    }

    @Override
    public void run() {
        while (true) {

            if (process == null) {
                synchronized (notificator) {
                    if (process == null) {
                        ConcurrentUtils.waitInSynchronize(notificator);
                    }
                }
            }

            try {
                process.waitFor();
            } catch (final InterruptedException e) {
                LOG.warn(e);
            }

            process = null;
            ready = false;
            setServer(null);
        }
    }

    /**
     * Set the last path to jMB.
     *
     * @param lastJmbPath the last path to jMB.
     */
    private void setLastJmbPath(@Nullable final Path lastJmbPath) {
        this.lastJmbPath = lastJmbPath;
    }

    /**
     * Get the last path to jMB.
     *
     * @return the last path to jMB.
     */
    private @Nullable Path getLastJmbPath() {
        return lastJmbPath;
    }

    /**
     * Start an instance of jMB.
     *
     * @param project the project which request starting an instance.
     */
    private void startInstance(@NotNull final Project project) {

        if (ready) {
            return;
        }

        final String title = JmeMessagesBundle.message("jmb.instance.launch.title");
        ProgressManager.getInstance().run(new Task.Modal(project, title, false) {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                startInstanceImpl();
            }
        });
    }

    /**
     * Execute starting jMB.
     */
    private synchronized void startInstanceImpl() {

        if (ready) {
            return;
        }

        final Path pathToJmb = JmePluginUtils.getPathToJmb();
        if (wasFailed && pathToJmb != null && pathToJmb.equals(getLastJmbPath())) {
            SwingUtilities.invokeLater(() -> {
                final String message = JmeMessagesBundle.message("jme.instance.error.wasFailed.message");
                final String title = JmeMessagesBundle.message("jme.instance.error.wasFailed.title");
                Messages.showWarningDialog(message, title);
            });
            return;
        }

        setLastJmbPath(pathToJmb);

        if (pathToJmb == null) {
            SwingUtilities.invokeLater(() -> {
                final String message = JmeMessagesBundle.message("jme.instance.error.noPath.message");
                final String title = JmeMessagesBundle.message("jme.instance.error.noPath.title");
                Messages.showWarningDialog(message, title);
            });
            wasFailed = true;
            return;
        }

        if (!JmePluginUtils.checkJmb(pathToJmb)) {
            return;
        }

        final int freePort = Utils.getFreePort(5000);

        final JmeModuleComponent moduleComponent = module.getComponent(JmeModuleComponent.class);
        final Path assetFolder = moduleComponent.getAssetFolder();

        final ProcessBuilder builder;

        if ("jar".equals(FileUtils.getExtension(pathToJmb))) {
            final Path folder = pathToJmb.getParent();
            builder = new ProcessBuilder("java", "-jar", pathToJmb.toString());
            builder.directory(folder.toFile());
        } else {
            builder = new ProcessBuilder(pathToJmb.toString());
        }

        final Map<String, String> env = builder.environment();
        env.put("Server.api.port", String.valueOf(freePort));

        if (assetFolder != null) {
            env.put("Editor.assetFolder", assetFolder.toString());
        }

        final Process process;
        try {
            process = builder.start();
        } catch (final IOException e) {
            LOG.warn(e);
            SwingUtilities.invokeLater(() -> {
                final String message = JmeMessagesBundle.message("jme.instance.error.cantExecute.message", pathToJmb.toString());
                final String title = JmeMessagesBundle.message("jme.instance.error.cantExecute.title");
                Messages.showWarningDialog(message, title);
            });
            wasFailed = true;
            return;
        }

        Server server = getServer();
        if (server != null) {
            server.destroy();
        }

        while (true) {
            try {
                server = clientNetwork.connect(new InetSocketAddress(freePort));
                server.sendPacket(new InitClasspathClientCommand(moduleComponent.getCompileOutput(), moduleComponent.getLibraries()));
                setServer(server);
                break;
            } catch (final RuntimeException e) {
                ThreadUtils.sleep(1000);
            }
        }


        this.process = process;
        this.wasFailed = false;
        this.ready = true;

        ConcurrentUtils.notifyAll(notificator);
    }

    /**
     * Get the server of jMB.
     *
     * @return the server of jMB.
     */
    private @Nullable Server getServer() {
        return server;
    }

    /**
     * Set the server of jMB.
     *
     * @param server the server of jMB.
     */
    private void setServer(@Nullable final Server server) {
        this.server = server;
    }

    /**
     * Send the command to jMB.
     *
     * @param command the command.
     * @param project the project.
     */
    public void sendCommand(@NotNull final ClientCommand command, @NotNull final Project project) {
        EXECUTOR_SERVICE.execute(() -> {
            startInstance(project);
            final Server server = getServer();
            if (server != null) {
                server.sendPacket(command);
            }
        });
    }

    /**
     * Send the command to jMB if we already have running instance.
     *
     * @param command the command.
     */
    public void sendCommandIfRunning(@NotNull final ClientCommand command) {
        if (!ready) return;
        EXECUTOR_SERVICE.execute(() -> {
            final Server server = getServer();
            if (server != null) {
                server.sendPacket(command);
            }
        });
    }
}
