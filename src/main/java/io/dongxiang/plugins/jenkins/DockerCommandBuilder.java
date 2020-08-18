package io.dongxiang.plugins.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import io.dongxiang.plugins.jenkins.command.DockerCommand;
import io.dongxiang.plugins.jenkins.command.DockerCommand.DockerCommandDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * Email: i@dongxiang.io
 * Github: https://github.com/idongxiang
 * Blog: https://blog.dongxiang.io
 *
 * @author dongxiang
 * @since 2020/7/31
 */
public class DockerCommandBuilder extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public DockerCommandBuilder() {
    }

    private DockerCommand dockerCommand;

    public DockerCommand getDockerCommand() {
        return dockerCommand;
    }

    @DataBoundSetter
    public void setDockerCommand(DockerCommand dockerCommand) {
        this.dockerCommand = dockerCommand;
    }

    private String dockerUrl;

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if (StringUtils.isBlank(build.getBuildVariables().get("dockerUrl"))) {
            listener.getLogger().println("Docker URL Parameter is not set, docker client won't be initialized");
            return false;
        }
        this.dockerUrl = build.getBuildVariables().get("dockerUrl");
        return super.prebuild(build, listener);
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {
        dockerCommand.setDockerUrl(dockerUrl);
        dockerCommand.execute(run, workspace, launcher, listener);
    }

    @Symbol("dockerCommand")
    @Extension
    public static final class DockerCommandBuilderDescriptorImpl extends BuildStepDescriptor<Builder> {
        private String dockerCommandGlobalConfig;

        public String getDockerCommandGlobalConfig() {
            return dockerCommandGlobalConfig;
        }

        public DockerCommandBuilderDescriptorImpl() {
            // 从磁盘加载数据到此对象
            load();
            // instance
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            // 全局配置提交触发此方法
            this.dockerCommandGlobalConfig = json.getString("dockerCommandGlobalConfig");
            // 保存到磁盘
            save();
            // instance
            return super.configure(req, json);
        }

        public DescriptorExtensionList<DockerCommand, DockerCommandDescriptor> getCommandDescriptors() {
            return DockerCommand.all();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Docker Remote Command";
        }
    }
}
