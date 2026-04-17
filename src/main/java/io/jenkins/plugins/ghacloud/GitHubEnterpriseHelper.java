package io.jenkins.plugins.ghacloud;

import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github.config.GitHubPluginConfig;
import org.jenkinsci.plugins.github.config.GitHubServerConfig;

/**
 * Isolated helper so that GitHub plugin classes are only loaded when the plugin is present.
 */
class GitHubEnterpriseHelper {

    private static GitHubPluginConfig getConfig() {
        return Jenkins.get().getDescriptorByType(GitHubPluginConfig.class);
    }

    static boolean hasEnterpriseServers() {
        GitHubPluginConfig config = getConfig();
        if (config != null) {
            for (GitHubServerConfig server : config.getConfigs()) {
                if (!"https://api.github.com".equals(server.getApiUrl())) {
                    return true;
                }
            }
        }
        return false;
    }

    static void addServers(ListBoxModel model) {
        GitHubPluginConfig config = getConfig();
        if (config != null) {
            for (GitHubServerConfig server : config.getConfigs()) {
                String url = server.getApiUrl();
                if (!"https://api.github.com".equals(url)) {
                    model.add(server.getDisplayName(), url);
                }
            }
        }
    }
}
