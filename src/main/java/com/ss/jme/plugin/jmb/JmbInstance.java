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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
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
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private volatile Server server;

    /**
     * The last used path to jMB.
     */
    @Nullable
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private volatile Path lastJmbPath;

    /**
     * The flag of reading to work with jMB.
     */
    private volatile boolean ready;

    /**
     * The flag about that last attempt was failed.
     */
    private volatile boolean wasFailed;

    public JmbInstance(@NotNull Module module) {
        this.module = module;
        this.notificator = new Object();
        this.clientNetwork = NetworkFactory.newDefaultAsyncClientNetwork(
                NETWORK_CONFIG, PACKET_REGISTRY, ConnectHandler.newDefault());
        start();
    }

    @Override
    public void run() {

        LOG.debug("Started background checking thread.");

        while (true) {

            if (process == null) {
                synchronized (notificator) {
                    if (process == null) {
                        LOG.debug("Waiting for a new jMB instance...");
                        ConcurrentUtils.waitInSynchronize(notificator);
                    }
                }
            }

            LOG.debug("Taken the jMB process: ", process);
            LOG.debug("Started waiting for finishing of the process: ", process);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                LOG.warn(e);
            }

            process = null;
            ready = false;
            setServer(null);
        }
    }

    /**
     * Starts an instance of jMB.
     *
     * @param project the project which request starting an instance.
     */
    private void startInstance(@NotNull Project project) {

        if (ready) {
            return;
        }

        String title = JmeMessagesBundle.message("jmb.instance.launch.title");
        ProgressManager.getInstance().run(new Task.Modal(project, title, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                startInstanceImpl();
            }
        });
    }

    /**
     * Executes starting jMB.
     */
    private synchronized void startInstanceImpl() {

        if (ready) {
            return;
        }

        Path pathToJmb = JmePluginUtils.getPathToJmb();
        if (wasFailed && pathToJmb != null && pathToJmb.equals(getLastJmbPath())) {
            SwingUtilities.invokeLater(() -> {
                String message = JmeMessagesBundle.message("jme.instance.error.wasFailed.message");
                String title = JmeMessagesBundle.message("jme.instance.error.wasFailed.title");
                Messages.showWarningDialog(message, title);
            });
            return;
        }

        setLastJmbPath(pathToJmb);

        if (pathToJmb == null) {
            SwingUtilities.invokeLater(() -> {
                String message = JmeMessagesBundle.message("jme.instance.error.noPath.message");
                String title = JmeMessagesBundle.message("jme.instance.error.noPath.title");
                Messages.showWarningDialog(message, title);
            });
            wasFailed = true;
            return;
        }

        if (!JmePluginUtils.checkJmb(pathToJmb)) {
            return;
        }

        LOG.debug("starting a new jMB instance...");

        int freePort = Utils.getFreePort(5000);

        LOG.debug("free port: ", freePort);

        JmeModuleComponent moduleComponent = module.getComponent(JmeModuleComponent.class);
        Path assetFolder = moduleComponent.getAssetFolder();

        LOG.debug("asset folder: ", assetFolder);

        ProcessBuilder builder;

        if ("jar".equals(FileUtils.getExtension(pathToJmb))) {
            Path folder = pathToJmb.getParent();
            builder = new ProcessBuilder("java", "-jar", pathToJmb.toString());
            builder.directory(folder.toFile());
        } else {
            builder = new ProcessBuilder(pathToJmb.toString());
        }

        Map<String, String> env = builder.environment();
        env.put("Server.api.port", String.valueOf(freePort));

        if (assetFolder != null) {
            env.put("Editor.assetFolder", assetFolder.toString());
        }

        builder.inheritIO();


        LOG.debug("commands: ", builder.command());
        LOG.debug("env: ", env);

        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            LOG.warn(e);
            SwingUtilities.invokeLater(() -> {
                String message = JmeMessagesBundle.message("jme.instance.error.cantExecute.message", pathToJmb.toString());
                String title = JmeMessagesBundle.message("jme.instance.error.cantExecute.title");
                Messages.showWarningDialog(message, title);
            });
            wasFailed = true;
            return;
        }

        Server server = getServer();
        if (server != null) {
            LOG.debug("destroy the previous server: ", server);
            server.destroy();
        }

        ThreadUtils.sleep(2000);

        LOG.debug("connecting to the launched instance...");

        while (true) {
            try {
                LOG.debug("Trying to connect...");
                server = clientNetwork.connect(new InetSocketAddress("localhost", freePort));
                server.sendPacket(new InitClasspathClientCommand(moduleComponent.getCompileOutput(), moduleComponent.getLibraries()));
                setServer(server);
                break;
            } catch (RuntimeException e) {
                LOG.warn(e);
                LOG.debug("Waiting for 1 sec.");
                ThreadUtils.sleep(1000);
            }
        }

        LOG.debug("Connected to the instance.");

        this.process = process;
        this.wasFailed = false;
        this.ready = true;

        LOG.debug("Notify background thread.");

        ConcurrentUtils.notifyAll(notificator);

        LOG.debug("jMB was started successfully.");
    }

    /**
     * Gets the server of jMB.
     *
     * @return the server of jMB.
     */
    private @NotNull Optional<Server> getServerOpt() {
        return Optional.ofNullable(server);
    }

    /**
     * Sends the command to jMB.
     *
     * @param command the command.
     * @param project the project.
     */
    public void sendCommand(@NotNull ClientCommand command, @NotNull Project project) {
        EXECUTOR_SERVICE.execute(() -> {
            startInstance(project);
            getServerOpt().ifPresent(it -> it.sendPacket(command));
        });
    }

    /**
     * Sends the command to jMB if we already have running instance.
     *
     * @param command the command.
     */
    public void sendCommandIfRunning(@NotNull ClientCommand command) {
        if (!ready) return;
        EXECUTOR_SERVICE.execute(() ->
                getServerOpt().ifPresent(it -> it.sendPacket(command)));
    }
}
