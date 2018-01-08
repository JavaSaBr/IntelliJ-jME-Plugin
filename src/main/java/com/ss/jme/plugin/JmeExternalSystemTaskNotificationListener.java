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

    public JmeExternalSystemTaskNotificationListener(@NotNull final Module module) {
        this.module = module;
        ServiceManager.getService(ExternalSystemProgressNotificationManager.class)
                .addNotificationListener(this);
    }

    @Override
    public void onSuccess(@NotNull final ExternalSystemTaskId id) {

        final ExternalSystemTaskType type = id.getType();
        final ProjectSystemId projectSystemId = id.getProjectSystemId();
        if (type != ExternalSystemTaskType.RESOLVE_PROJECT || GradleConstants.SYSTEM_ID != projectSystemId) {
            return;
        }

        final Project project = id.findProject();
        final Project moduleProject = module.getProject();
        if (project != moduleProject) {
            return;
        }

        final JmeModuleComponent moduleComponent = module.getComponent(JmeModuleComponent.class);
        moduleComponent.onProjectResolved();
    }

    @Override
    public void onFailure(@NotNull final ExternalSystemTaskId id, @NotNull final Exception e) {

    }

    @Override
    public void beforeCancel(@NotNull final ExternalSystemTaskId id) {

    }

    @Override
    public void onCancel(@NotNull final ExternalSystemTaskId id) {

    }
    @Override
    public void onQueued(@NotNull final ExternalSystemTaskId id, final String workingDir) {

    }

    @Override
    public void onStart(@NotNull final ExternalSystemTaskId id) {

    }

    @Override
    public void onStatusChange(@NotNull final ExternalSystemTaskNotificationEvent event) {

    }

    @Override
    public void onTaskOutput(@NotNull final ExternalSystemTaskId id, @NotNull final String text, final boolean stdOut) {

    }

    @Override
    public void onEnd(@NotNull final ExternalSystemTaskId id) {

    }

    /**
     * Dispose this listener.
     */
    public void dispose() {
        ServiceManager.getService(ExternalSystemProgressNotificationManager.class)
                .removeNotificationListener(this);
    }
}
