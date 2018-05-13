package com.ss.editor.plugin.example;

import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.EditorPlugin;
import com.ss.editor.plugin.example.PluginMessages;
import com.ss.editor.ui.component.asset.tree.AssetTreeContextMenuFillerRegistry;
import com.ss.rlib.common.plugin.PluginContainer;
import com.ss.rlib.common.plugin.annotation.PluginDescription;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of {@link EditorPlugin}.
 */
@PluginDescription(
        id = "com.ss.editor.plugin.example",
        version = "1.0",
        minAppVersion = "1.7.0",
        name = "Example plugin",
        description = "Example plugin"
)
public class ExamplePlugin extends EditorPlugin {

    public ExamplePlugin(@NotNull final PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final AssetTreeContextMenuFillerRegistry registry) {
        super.register(registry);

        registry.registerSingle((element, items, actionTester) -> {

            final MenuItem item = new MenuItem(PluginMessages.HELLO_ACTION);
            item.setOnAction(event -> System.out.println("Hello"));

            items.add(item);
        });
    }
}
