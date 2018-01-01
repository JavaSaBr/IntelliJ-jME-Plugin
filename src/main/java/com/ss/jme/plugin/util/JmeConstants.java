package com.ss.jme.plugin.util;

import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The list of plugin constants.
 *
 * @author JavaSaBr
 */
public interface JmeConstants {

    @NonNls
    @NotNull ProjectSystemId SYSTEM_ID = new ProjectSystemId("jMonkeyEngine");
}
