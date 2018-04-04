package com.ss.jme.plugin.jmb.command.client;

import com.ss.rlib.network.annotation.PacketDescription;
import com.ss.rlib.util.array.Array;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * The command to load module classpath in jMB.
 *
 * @author JavaSaBr
 */
@PacketDescription(id = 4)
public class InitClasspathClientCommand extends ClientCommand {

    /**
     * The libraries.
     */
    @NotNull
    private final Array<Path> libraries;

    /**
     * The output folder.
     */
    @Nullable
    private final Path output;

    public InitClasspathClientCommand(@Nullable Path output, @NotNull Array<Path> libraries) {
        this.output = output;
        this.libraries = libraries;
    }

    @Override
    protected void writeImpl(@NotNull ByteBuffer buffer) {
        super.writeImpl(buffer);
        writeInt(buffer, libraries.size());
        libraries.forEach(library -> writeString(buffer, library.toString()));
        writeString(buffer, output == null ? "" : output.toString());
    }
}
