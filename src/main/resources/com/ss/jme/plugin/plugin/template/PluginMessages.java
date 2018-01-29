package com.ss.editor.plugin.example;

import com.ss.editor.plugin.api.messages.MessagesPluginFactory;

import java.util.ResourceBundle;

/**
 * The class with localised all plugin messages.
 *
 * @author JavaSaBr
 */
public interface PluginMessages {

    ResourceBundle RESOURCE_BUNDLE = MessagesPluginFactory.getResourceBundle(ExamplePlugin.class,
            "plugin/example/messages/messages");

    String HELLO_ACTION = RESOURCE_BUNDLE.getString("HelloAction");
}
