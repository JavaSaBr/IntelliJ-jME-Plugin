package com.ss.jme.plugin.jmb.command.server;

import com.ss.rlib.common.network.ConnectionOwner;
import com.ss.rlib.common.network.annotation.PacketDescription;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@PacketDescription(id = 1)
public class EmptyServerCommand extends ServerCommand {

    @Override
    protected void readImpl(@NotNull ConnectionOwner owner, @NotNull ByteBuffer buffer) {

    }
}
