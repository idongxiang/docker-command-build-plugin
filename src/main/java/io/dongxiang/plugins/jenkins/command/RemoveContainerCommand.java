package io.dongxiang.plugins.jenkins.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Email: i@dongxiang.io
 * Github: https://github.com/idongxiang
 * Blog: https://blog.dongxiang.io
 *
 * @author dongxiang
 * @since 2020/7/31
 */
public class RemoveContainerCommand extends DockerCommand {

    @DataBoundConstructor
    public RemoveContainerCommand(String containerId) {
        this.containerId = containerId;
    }

    private final String containerId;

    public String getContainerId() {
        return containerId;
    }

    @Override
    public void execute(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        EnvVars envVars = run.getEnvironment(listener);
        String _containerId = envVars.expand(containerId);
        DockerClient dockerClient = getDockerClient();
        RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(_containerId);
        try {
            listener.getLogger().println("try remove container " + _containerId);
            removeContainerCmd.exec();
            listener.getLogger().println("removed container " + _containerId);
        } catch (NotFoundException e) {
            listener.getLogger().println("remove container " + _containerId + " not found");
        }
    }

    @Extension
    public static final class RemoveContainerDescriptorImpl extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Remove Container";
        }
    }

}
