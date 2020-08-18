package io.dongxiang.plugins.jenkins;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

/**
 * @author dongxiang.io
 * @since 2020-08-04
 */
public class DockerUrlParameterDefinition extends SimpleParameterDefinition {

    private final String defaultUrl;

    @DataBoundConstructor
    public DockerUrlParameterDefinition(String name, String description, String defaultUrl) {
        super(name, description);
        this.defaultUrl = defaultUrl;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    @Override
    public ParameterValue getDefaultParameterValue() {
        return new StringParameterValue(getName(), getDefaultUrl(), getDescription());
    }

    @Override
    public ParameterValue createValue(String value) {
        return new StringParameterValue(getName(), value);
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        return req.bindJSON(StringParameterValue.class, jo);
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Docker Url Parameter";
        }
    }
}
