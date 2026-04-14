package io.jenkins.plugins.ghacloud;

import hudson.slaves.AbstractCloudComputer;

public class GitHubActionsComputer extends AbstractCloudComputer<GitHubActionsSlave> {

    public GitHubActionsComputer(GitHubActionsSlave slave) {
        super(slave);
    }
}
