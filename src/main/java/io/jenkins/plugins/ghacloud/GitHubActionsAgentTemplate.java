package io.jenkins.plugins.ghacloud;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.util.Set;

import hudson.model.Describable;

public class GitHubActionsAgentTemplate implements Describable<GitHubActionsAgentTemplate> {

    private final String labelString;
    private final String remoteFs;
    private final int numExecutors;
    private final String gitRef;
    private final int idleMinutes;
    private final String workflowFileName;
    private final int agentCap;

    @DataBoundConstructor
    public GitHubActionsAgentTemplate(String labelString, String remoteFs,
                                      int numExecutors, String gitRef, int idleMinutes,
                                      String workflowFileName, int agentCap) {
        this.labelString = labelString;
        this.remoteFs = (remoteFs != null && !remoteFs.isEmpty()) ? remoteFs : "/home/runner/agent";
        this.numExecutors = numExecutors > 0 ? numExecutors : 1;
        this.gitRef = (gitRef != null && !gitRef.isEmpty()) ? gitRef : "main";
        this.idleMinutes = idleMinutes > 0 ? idleMinutes : 5;
        this.workflowFileName = workflowFileName;
        this.agentCap = agentCap;
    }

    public String getLabelString() {
        return labelString;
    }

    public String getRemoteFs() {
        return remoteFs;
    }

    public int getNumExecutors() {
        return numExecutors;
    }

    public String getGitRef() {
        return gitRef;
    }

    public int getIdleMinutes() {
        return idleMinutes;
    }

    public String getWorkflowFileName() {
        return workflowFileName;
    }

    public int getAgentCap() {
        return agentCap;
    }

    public boolean matches(Label label) {
        if (label == null) {
            return true;
        }
        if (labelString == null || labelString.isEmpty()) {
            return false;
        }
        Set<LabelAtom> labelAtoms = Label.parse(labelString);
        return label.matches(labelAtoms);
    }

    public boolean matches(GitHubActionsAgent agent) {
        if (labelString == null || labelString.isEmpty()) {
            return true;
        }
        Set<LabelAtom> templateLabels = Label.parse(labelString);
        Set<LabelAtom> agentLabels = agent.getAssignedLabels().stream()
                .filter(l -> l instanceof LabelAtom)
                .map(l -> (LabelAtom) l)
                .collect(java.util.stream.Collectors.toSet());
        return agentLabels.containsAll(templateLabels);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<GitHubActionsAgentTemplate> {

        @Override
        public String getDisplayName() {
            return "GitHub Actions Agent Template";
        }

        @RequirePOST
        public FormValidation doCheckLabelString(@QueryParameter String value) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.warning("No labels set — this template will match any label request");
            }
            return FormValidation.ok();
        }

        @RequirePOST
        public FormValidation doCheckWorkflowFileName(@QueryParameter String value) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("Workflow file name is required (e.g. jenkins-agent.yml)");
            }
            return FormValidation.ok();
        }
    }
}
