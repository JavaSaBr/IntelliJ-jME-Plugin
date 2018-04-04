package com.ss.jme.plugin;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemProgressNotificationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

/**
 * The listener to listen results of external tasks.
 *
 * @author JavaSaBr
 */
public class JmeExternalSystemTaskNotificationListener implements ExternalSystemTaskNotificationListener {

    @NotNull
    private final Module module;

    public JmeExternalSystemTaskNotificationListener(@NotNull Module module) {
        this.module = module;
        ServiceManager.getService(ExternalSystemProgressNotificationManager.class)
                .addNotificationListener(this);
    }

    @Override
    public void onSuccess(@NotNull ExternalSystemTaskId id) {

        ExternalSystemTaskType type = id.getType();
        ProjectSystemId projectSystemId = id.getProjectSystemId();
        if (type != ExternalSystemTaskType.RESOLVE_PROJECT || GradleConstants.SYSTEM_ID != projectSystemId) {
            return;
        }

        Project project = id.findProject();
        Project moduleProject = module.getProject();
        if (project != moduleProject) {
            return;
        }

        module.getComponent(JmeModuleComponent.class)
                .onProjectResolved();
    }

    @Override
    public void onFailure(@NotNull ExternalSystemTaskId id, @NotNull Exception e) {

    }

    @Override
    public void beforeCancel(@NotNull ExternalSystemTaskId id) {

    }

    @Override
    public void onCancel(@NotNull ExternalSystemTaskId id) {

    }
    @Override
    public void onQueued(@NotNull ExternalSystemTaskId id, String workingDir) {

    }

    @Override
    public void onStart(@NotNull ExternalSystemTaskId id) {

    }

    @Override
    public void onStatusChange(@NotNull ExternalSystemTaskNotificationEvent event) {

    }

    @Override
    public void onTaskOutput(@NotNull ExternalSystemTaskId id, @NotNull String text, boolean stdOut) {

    }

    @Override
    public void onEnd(@NotNull ExternalSystemTaskId id) {

    }

    /**
     * Dispose this listener.
     */
    public void dispose() {
        ServiceManager.getService(ExternalSystemProgressNotificationManager.class)
                .removeNotificationListener(this);
    }
}
