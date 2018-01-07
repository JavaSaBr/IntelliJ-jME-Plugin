package com.ss.jme.plugin.jmb;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.ss.jme.plugin.JmeMessagesBundle;
import com.ss.jme.plugin.JmeModuleComponent;
import com.ss.jme.plugin.jmb.command.client.ClientCommand;
import com.ss.jme.plugin.jmb.command.server.EmptyServerCommand;
import com.ss.jme.plugin.util.JmePluginUtils;
import com.ss.rlib.concurrent.util.ConcurrentUtils;
import com.ss.rlib.network.NetworkFactory;
import com.ss.rlib.network.client.ClientNetwork;
import com.ss.rlib.network.client.server.Server;
import com.ss.rlib.network.packet.ReadablePacketRegistry;
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
        this.clientNetwork = NetworkFactory.newDefaultAsyncClientNetwork(PACKET_REGISTRY);
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
            server = null;
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
     */
    private synchronized void startInstance() {
        if (ready) return;

        final Path pathToJmb = JmePluginUtils.getPathToJmb();
        if (wasFailed && pathToJmb != null && pathToJmb.equals(getLastJmbPath())) {
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

        final ProcessBuilder builder = new ProcessBuilder(pathToJmb.toString());
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

        final Server server = getServer();
        if (server != null) {
            server.destroy();
        }

        this.process = process;
        this.server = clientNetwork.connect(new InetSocketAddress(freePort));
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
     * Send the command to jMB.
     *
     * @param command the command.
     */
    public synchronized void sendCommand(@NotNull final ClientCommand command) {
        EXECUTOR_SERVICE.execute(() -> {
            startInstance();
            final Server server = getServer();
            if (server != null) {
                server.sendPacket(command);
            }
        });
    }
}
