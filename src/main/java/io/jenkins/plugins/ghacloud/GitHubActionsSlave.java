package io.jenkins.plugins.ghacloud;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.JNLPLauncher;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitHubActionsSlave extends AbstractCloudSlave {

    private static final Logger LOGGER = Logger.getLogger(GitHubActionsSlave.class.getName());

    private final String cloudName;

    @DataBoundConstructor
    public GitHubActionsSlave(String name, String remoteFs, String labelString,
                              int numExecutors, int idleMinutes, String cloudName)
            throws Descriptor.FormException, IOException {
        super(name, remoteFs, new JNLPLauncher());
        this.cloudName = cloudName;
        setNumExecutors(numExecutors);
        setMode(Node.Mode.EXCLUSIVE);
        setLabelString(labelString);
        setRetentionStrategy(new GitHubActionsRetentionStrategy(idleMinutes));
    }

    public String getCloudName() {
        return cloudName;
    }

    @Override
    public AbstractCloudComputer<?> createComputer() {
        return new GitHubActionsComputer(this);
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        LOGGER.log(Level.INFO, "Terminating GitHub Actions agent: {0}", getNodeName());
        // The GitHub Actions workflow terminates when the agent process exits.
        // No explicit cleanup is needed — the GHA runner will detect the
        // disconnection and the workflow job will complete.
    }

    @Extension
    public static class DescriptorImpl extends Slave.SlaveDescriptor {

        @Override
        public String getDisplayName() {
            return "GitHub Actions Agent";
        }

        @Override
        public boolean isInstantiable() {
            // Agents are only created via cloud provisioning, not manually
            return false;
        }
    }
}
