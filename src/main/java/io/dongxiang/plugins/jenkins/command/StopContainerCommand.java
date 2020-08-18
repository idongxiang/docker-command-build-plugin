package io.dongxiang.plugins.jenkins.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
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
public class StopContainerCommand extends DockerCommand {
    @DataBoundConstructor
    public StopContainerCommand(String containerId) {
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
        StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(_containerId);
        try {
            listener.getLogger().println("try stop container " + _containerId);
            stopContainerCmd.exec();
            listener.getLogger().println("stop container " + _containerId + " success");
        } catch (NotFoundException e) {
            listener.getLogger().println("stop container " + _containerId + " not found");
        } catch (NotModifiedException e) {
            listener.getLogger().println("stop container " + _containerId + " not modified");
        }
    }

    @Extension
    public static final class StopContainerDescriptorImpl extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Stop Container";
        }
    }
}
