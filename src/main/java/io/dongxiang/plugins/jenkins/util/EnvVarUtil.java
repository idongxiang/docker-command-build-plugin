package io.dongxiang.plugins.jenkins.util;

import hudson.EnvVars;

/**
 * @author dongxiang
 * @since 2020-08-04
 */
public class EnvVarUtil {

    public static String res(EnvVars envVars, String s) {
        return envVars.expand(s);
    }

}
