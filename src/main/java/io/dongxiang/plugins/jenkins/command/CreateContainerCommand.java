package io.dongxiang.plugins.jenkins.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
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
public class CreateContainerCommand extends DockerCommand {

    @DataBoundConstructor
    public CreateContainerCommand(String image, String name, String portBindings,
                                  String volume, String cmd) {
        this.image = image;
        this.name = name;
        this.portBindings = portBindings;
        this.volume = volume;
        this.cmd = cmd;
    }

    private final String image;
    private final String portBindings;
    private final String volume;
    private final String cmd;
    private final String name;

    public String getImage() {
        return image;
    }

    public String getPortBindings() {
        return portBindings;
    }

    public String getVolume() {
        return volume;
    }

    public String getCmd() {
        return cmd;
    }

    public String getName() {
        return name;
    }

    @Override
    public void execute(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        EnvVars envVars = run.getEnvironment(listener);
        String _portBindings = envVars.expand(portBindings);
        String _volume = envVars.expand(volume);
        String _image = envVars.expand(image);
        String _cmd = envVars.expand(cmd);
        String _name = envVars.expand(name);

        DockerClient dockerClient = getDockerClient();
        PortBinding portBinding = PortBinding.parse(_portBindings);
        Bind bind = Bind.parse(_volume);
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(bind)
                .withPortBindings(portBinding);
        listener.getLogger().println("try create container from image " + _image);
        CreateContainerResponse response = dockerClient.createContainerCmd(_image)
                .withCmd(_cmd)
                .withName(_name)
                .withHostConfig(hostConfig)
                .exec();
        String containerId = response.getId();
        listener.getLogger().println("created container " + containerId + " from image " + _image);
    }

    @Extension
    public static final class CreateContainerDescriptorImpl extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Create Container";
        }
    }
}
