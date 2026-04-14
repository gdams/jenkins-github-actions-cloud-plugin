package io.jenkins.plugins.ghacloud;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.RetentionStrategy;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitHubActionsRetentionStrategy extends RetentionStrategy<AbstractCloudComputer<?>> {

    private static final Logger LOGGER = Logger.getLogger(GitHubActionsRetentionStrategy.class.getName());

    private final int idleMinutes;

    @DataBoundConstructor
    public GitHubActionsRetentionStrategy(int idleMinutes) {
        this.idleMinutes = Math.max(idleMinutes, 1);
    }

    public int getIdleMinutes() {
        return idleMinutes;
    }

    @Override
    public long check(AbstractCloudComputer<?> c) {
        if (c.isIdle()) {
            long idleMs = System.currentTimeMillis() - c.getIdleStartMilliseconds();
            if (idleMs > (long) idleMinutes * 60 * 1000) {
                LOGGER.log(Level.INFO, "Agent {0} has been idle for {1} minutes, terminating",
                        new Object[]{c.getName(), idleMinutes});
                try {
                    AbstractCloudSlave node = (AbstractCloudSlave) c.getNode();
                    if (node != null) {
                        node.terminate();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Interrupted while terminating agent " + c.getName(), e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to terminate agent " + c.getName(), e);
                }
            }
        }
        return 1; // re-check every minute
    }

    @Override
    public void start(AbstractCloudComputer<?> c) {
        c.connect(false);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {

        @Override
        public String getDisplayName() {
            return "GitHub Actions Cloud Retention Strategy";
        }
    }
}
