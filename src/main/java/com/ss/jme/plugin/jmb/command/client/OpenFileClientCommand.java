package com.ss.jme.plugin.jmb.command.client;

import com.ss.rlib.common.network.annotation.PacketDescription;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * The command to open a file in jMB.
 *
 * @author JavaSaBr
 */
@PacketDescription(id = 1)
public class OpenFileClientCommand extends ClientCommand {

    /**
     * The asset folder.
     */
    @NotNull
    private final String assetFolder;

    /**
     * The file to open.
     */
    @NotNull
    private final String file;

    public OpenFileClientCommand(@NotNull Path assetFolder, @NotNull Path file) {
        this.assetFolder = assetFolder.toString();
        this.file = file.toString();
    }

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        super.writeImpl(buffer);
        writeString(buffer, assetFolder);
        writeString(buffer, file);
    }
}
