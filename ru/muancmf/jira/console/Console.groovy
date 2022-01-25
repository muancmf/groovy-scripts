package ru.muancmf.jira.console

import ru.muancmf.jira.Configurations.Configuration
import ru.muancmf.jira.RemoteJira
import ru.muancmf.jira.Operations.Operations

def remoteJiraHost = "http://localhost:8080"
def remoteJiraAuthentication = ""
def jqlForRemoteIssues = "project = REMOTE"
def remoteJiraUrlQuery = [expand: "key,summary,description", jql: jqlForRemoteIssues]
def jqlPrefixForLocalIssues = "project = LOCAL and %s ~ %s"
def fieldWithLink = "externalId"

def remoteJira = new RemoteJira(remoteJiraHost, remoteJiraAuthentication)
remoteJira.fetchAllIssuesForJql(remoteJiraUrlQuery)
def parsedDataFromRemoteJira = remoteJira.handleResponses({
    it.issues.collect {
        [
                (fieldWithLink): it.key,
                summary        : it.fields.summary,
                description    : it.fields.description
        ]
    }
})

parsedDataFromRemoteJira.each {
    def localIssue = Operations.getIssueForJql(String.format(jqlPrefixForLocalIssues, fieldWithLink, it[fieldWithLink]))
    if (localIssue != null) {
        def operations = Configuration.fields[it.key]
        it.each {
            if (!operations.isSame(localIssue, it.value)) {
                operations.setValue(localIssue, it.value)
            }
        }
    } else {
        Operations.createIssue(it)
    }
}

println(parsedDataFromRemoteJira)


