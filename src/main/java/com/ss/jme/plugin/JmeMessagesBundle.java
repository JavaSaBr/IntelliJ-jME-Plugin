package com.ss.jme.plugin;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * The bundle of this plugin.
 *
 * @author JavaSaBr
 */
public class JmeMessagesBundle extends AbstractBundle {

    private static final JmeMessagesBundle INSTANCE = new JmeMessagesBundle();

    private JmeMessagesBundle() {
        super("messages.jme");
    }

    /**
     * Gets the message.
     *
     * @param key    the message key.
     * @param params the params.
     * @return the message.
     */
    public static @NotNull String message(@NotNull @PropertyKey(resourceBundle = "messages.jme") String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
