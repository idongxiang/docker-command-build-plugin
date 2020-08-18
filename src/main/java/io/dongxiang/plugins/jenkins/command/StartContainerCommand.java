package io.dongxiang.plugins.jenkins.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StartContainerCmd;
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
public class StartContainerCommand extends DockerCommand {

    @DataBoundConstructor
    public StartContainerCommand(String containerId) {
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
        StartContainerCmd startContainerCmd = dockerClient.startContainerCmd(_containerId);
        try {
            listener.getLogger().println("try start container " + _containerId);
            startContainerCmd.exec();
            listener.getLogger().println("started container " + _containerId);
        } catch (NotFoundException e) {
            listener.getLogger().println("start container " + _containerId + " not found");
        } catch (NotModifiedException e) {
            listener.getLogger().println("start container " + _containerId + " Not Modified");
        }
    }

    @Extension
    public static final class StartContainerDescriptorImpl extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Start Container";
        }
    }
}
