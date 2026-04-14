package io.jenkins.plugins.ghacloud;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Minimal GitHub REST API client for triggering workflow_dispatch events.
 */
public class GitHubClient {

    private static final Logger LOGGER = Logger.getLogger(GitHubClient.class.getName());

    private final String apiUrl;
    private final String token;
    private final HttpClient httpClient;

    public GitHubClient(String apiUrl, String token) {
        this.apiUrl = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        this.token = token;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Triggers a workflow_dispatch event on the specified repository and workflow file.
     *
     * @param repository     owner/repo
     * @param workflowFile   the workflow filename (e.g. jenkins-agent.yml)
     * @param ref            git ref to run against (e.g. main)
     * @param inputs         key-value inputs forwarded to the workflow
     */
    public void triggerWorkflow(String repository, String workflowFile, String ref,
                                Map<String, String> inputs) throws IOException {
        String url = apiUrl + "/repos/" + repository + "/actions/workflows/" + workflowFile + "/dispatches";

        String body = buildJson(ref, inputs);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();

        LOGGER.log(Level.FINE, "Dispatching workflow: POST {0}", url);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204) {
                throw new IOException("GitHub API returned HTTP " + response.statusCode()
                        + " for workflow dispatch: " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while calling GitHub API", e);
        }
    }

    private static String buildJson(String ref, Map<String, String> inputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ref\":\"").append(escapeJson(ref)).append("\"");
        if (inputs != null && !inputs.isEmpty()) {
            sb.append(",\"inputs\":{");
            boolean first = true;
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                        .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
