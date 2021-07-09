library "pipeline-shared-libraries@serviceCommitSharedPipeline-2.6.0"

serviceCommitSharedPipeline {
    agent_label = 'java-8'
    s3Bucket = 'vtw-repo'
    // services format - <app1Module:app1Context:app1CommitYaml,app2Module:app2Context:app2CommitYaml>
    services = 'vtw-common-event-logging:vtw-common-event-logging:vtw-common-event-logging.yml'
    ecr = '461549540087.dkr.ecr.eu-west-1.amazonaws.com'
    enableQualityGate = 'true'
    projectKey = 'vtw-common-event-logging'
    mvnItTestCommand = 'mvn clean verify -DVTWENV=DEVNP -Pnightly'
}
