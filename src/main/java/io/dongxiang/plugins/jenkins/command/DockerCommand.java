package io.dongxiang.plugins.jenkins.command;

import com.github.dockerjava.api.DockerClient;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.dongxiang.plugins.jenkins.client.DockerCommandClient;
import jenkins.model.Jenkins;

import java.io.IOException;

/**
 * Email: i@dongxiang.io
 * Github: https://github.com/idongxiang
 * Blog: https://blog.dongxiang.io
 *
 * @author dongxiang
 * @since 2020/7/31
 */
public abstract class DockerCommand implements Describable<DockerCommand>, ExtensionPoint {

    private String dockerUrl;

    public void setDockerUrl(String dockerUrl) {
        this.dockerUrl = dockerUrl;
    }

    public String getDockerUrl() {
        return dockerUrl;
    }

    public DockerCommandDescriptor getDescriptor() {
        return (DockerCommandDescriptor) Jenkins.get().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<DockerCommand, DockerCommandDescriptor> all() {
        return Jenkins.get().getDescriptorList(DockerCommand.class);
    }

    public DockerClient getDockerClient() {
        return DockerCommandClient.getDockerClient(getDockerUrl());
    }

    public abstract void execute(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException;

    public abstract static class DockerCommandDescriptor extends Descriptor<DockerCommand> {
        protected DockerCommandDescriptor(Class<? extends DockerCommand> clazz) {
            super(clazz);
        }

        protected DockerCommandDescriptor() {
        }
    }
}
