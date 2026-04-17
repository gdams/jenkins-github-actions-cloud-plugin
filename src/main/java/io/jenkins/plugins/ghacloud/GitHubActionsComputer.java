package io.jenkins.plugins.ghacloud;

import hudson.slaves.AbstractCloudComputer;

public class GitHubActionsComputer extends AbstractCloudComputer<GitHubActionsAgent> {

    public GitHubActionsComputer(GitHubActionsAgent agent) {
        super(agent);
    }
}
