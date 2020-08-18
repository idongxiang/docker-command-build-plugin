package io.dongxiang.plugins.jenkins.command;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import hudson.*;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

/**
 * Email: i@dongxiang.io
 * Github: https://github.com/idongxiang
 * Blog: https://blog.dongxiang.io
 *
 * @author dongxiang
 * @since 2020/7/31
 */
public class PullImageCommand extends DockerCommand {

    @DataBoundConstructor
    public PullImageCommand(String image, String tag, String registry, String repository,
                            String credentialsId,
                            String registryUrl) {
        this.image = image;
        this.tag = tag;
        this.registry = registry;
        this.repository = repository;
        this.credentialsId = credentialsId;
        this.registryUrl = registryUrl;
    }

    private final String image;
    private final String tag;
    private final String registry;
    private final String repository;
    private final String credentialsId;
    private final String registryUrl;

    public String getImage() {
        return image;
    }

    public String getTag() {
        return tag;
    }

    public String getRegistry() {
        return registry;
    }

    public String getRepository() {
        return repository;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    @Override
    public void execute(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        StandardUsernamePasswordCredentials standardUsernamePasswordCredentials = CredentialsProvider.findCredentialById(
                credentialsId,
                StandardUsernamePasswordCredentials.class,
                run,
                URIRequirementBuilder.fromUri(registry).build()
        );

        if (standardUsernamePasswordCredentials == null) {
            listener.getLogger().println("Cannot find credential " + credentialsId);
            throw new AbortException("Cannot find credential " + credentialsId);
        }

        EnvVars envVars = run.getEnvironment(listener);
        String _image = envVars.expand(image);
        String _tag = envVars.expand(tag);
        String _registry = envVars.expand(registry);
        String _repository = envVars.expand(repository);
        String _username = standardUsernamePasswordCredentials.getUsername();
        String _password = standardUsernamePasswordCredentials.getPassword().getPlainText();

        listener.getLogger().println("try pull image " + credentialsId + " tag " + _tag);

        DockerClient dockerClient = getDockerClient();

        String fromImage = _registry + "/" + _repository + "/" + _image;

        listener.getLogger().println("try pull image " + fromImage + " tag " + _tag);
        AuthConfig authConfig = new AuthConfig().withRegistryAddress(_registry)
                .withUsername(_username)
                .withPassword(_password);
        dockerClient.authCmd().withAuthConfig(authConfig).exec();
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(fromImage)
                .withAuthConfig(authConfig)
                .withRegistry(_registry)
                .withTag(_tag);
        PullImageResultCallback callback = new PullImageResultCallback() {
//            上：\u001b[{n}A
//            下：\u001b[{n}B
//            右：\u001b[{n}C
//            左：\u001b[{n}D
//            listener.getLogger().println("\u001b[" + index + "A");
//            listener.getLogger().println("\u001b[" + index + "B");

            @Override
            public void onNext(PullResponseItem item) {
                if (StringUtils.isNotBlank(item.getProgress())) {
                    listener.getLogger().println(item.getStatus() + " " + item.getProgress() + " " + item.getId());
                } else {
                    listener.getLogger().println(item.getStatus() + " " + item.getId());
                }
                super.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                listener.getLogger().println("[Error] pull image " + _image + " tag " + _tag + ", error=" + throwable.getMessage());
                super.onError(throwable);
            }
        };
        PullImageResultCallback resultCallback = pullImageCmd.exec(callback);
        resultCallback.awaitCompletion();
        listener.getLogger().println("pull image " + _image + " tag " + _tag + " success");

    }

    @Extension
    public static final class PullImageDescriptorImpl extends DockerCommandDescriptor {
        @Override
        public String getDisplayName() {
            return "Pull Image";
        }

        public ListBoxModel doFillCredentialsIdItems(
                @AncestorInPath Item item,
                @QueryParameter String credentialsId,
                @QueryParameter String registryUrl) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                    .includeEmptyValue()
                    .includeMatchingAs(
                            item instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) item) : ACL.SYSTEM,
                            item,
                            StandardUsernamePasswordCredentials.class,
                            URIRequirementBuilder.fromUri(registryUrl).build(),
                            CredentialsMatchers.allOf(CredentialsMatchers.allOf()))
                    .includeCurrentValue(credentialsId);
        }

        public FormValidation doCheckCredentialsId(
                @AncestorInPath Item item,
                @QueryParameter String value,
                @QueryParameter String registryUrl) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            if (value.startsWith("${") && value.endsWith("}")) {
                return FormValidation.warning("Cannot validate expression based credentials");
            }

            StandardUsernamePasswordCredentials standardUsernamePasswordCredentials = CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(
                    StandardUsernamePasswordCredentials.class,
                    item,
                    item instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) item) : ACL.SYSTEM,
                    URIRequirementBuilder.fromUri(registryUrl).build()
            ), CredentialsMatchers.withId(value));
            if (standardUsernamePasswordCredentials == null) {
                return FormValidation.error("Cannot find currently selected credentials");
            }

            return FormValidation.ok();
        }
    }
}
