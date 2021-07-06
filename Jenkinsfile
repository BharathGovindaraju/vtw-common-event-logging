library "pipeline-shared-libraries@serviceCommitSharedPipelineNonDocker-2.7.0"

serviceCommitSharedPipelineNonDocker {
    agent_label = 'java-8'
    s3Bucket = 'vtw-repo'
    //services format - <app1Module:app1Context:app1CommitYaml,app2Module:app2Context:app2CommitYaml>
    services = 'vtw-common-event-logging:event-logging.war:'
    dockerIgnore = 'vtw-common-event-logging'
    ecr = '461549540087.dkr.ecr.eu-west-1.amazonaws.com'
    mvnItTestCommand = 'mvn clean verify -DVTWENV=DEVNP -Pfull -fae'
    skipReleaseMetadata = 'false'
    enableQualityGate = 'true'
    projectKey = 'vtw-common-event-logging'
}
