package ru.muancmf.jira.Operations

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.query.Query
import ru.muancmf.jira.Configurations.Configuration

class Operations {

    static def getIssueForJql(String jql) {
        def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
        def searchService = ComponentAccessor.getComponent(SearchService.class)
        def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser.class)
        Query query = jqlQueryParser.parseQuery(jql)
        def results = searchService.search(loggedInUser, query, PagerFilter.getUnlimitedFilter())

        results.getResults().take(1).each { issue ->
            return issueManager.getIssueObject(issue.id)
        }
    }

    static void createIssue(Map remoteIssuesProperties) {
        def(addOnlyAfterIssueCreated, addInAnyTime) = remoteIssuesProperties.split {it.key in ["status", "label"]}
        def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
        MutableIssue newIssue = ComponentAccessor.getIssueFactory().getIssue()
        newIssue.setProjectId(100000L)
        newIssue.setIssueTypeId("10001")

        addInAnyTime.each {
            def operations = Configuration.fields[it.key]
            operations.setValue(newIssue, it.value)
        }
        def createdIssue = ComponentAccessor.getIssueManager().getIssueObject(ComponentAccessor.issueManager.createIssueObject(loggedInUser, newIssue).id)
        addOnlyAfterIssueCreated.each {
            def operations = Configuration.fields[it.key]
            operations.setValue(createdIssue, it.value)
        }
    }

    static void setSummary(MutableIssue issue, String value) {
        issue.setSummary(value)
    }

    static boolean isSameSummary(Issue issue, String value) {
        issue.getSummary().equals(value)
    }

    static void setDescription(MutableIssue issue, String value) {
        issue.setDescription(value)
    }

    static boolean isSameDescription(Issue issue, String value) {
        issue.getDescription().equals(value)
    }

    static void setTextCustomFieldValue(def issue, String fieldId, String value) {
        issue.setCustomFieldValue(ComponentAccessor.getCustomFieldManager()
                .getCustomFieldObject(fieldId), value)
    }

    static boolean isSameTextCustomFieldValue(def issue, String fieldId, String value) {
        issue.getCustomFieldValue(ComponentAccessor.getCustomFieldManager().getCustomFieldObject(fieldId))
                .equals(value)
    }

    static void setSelectCustomFieldValue(def issue, String fieldId, String value) {
        def customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(fieldId)
        issue.setCustomFieldValue(customField, getOptions(issue, customField, [value]).get(0))
    }

    static boolean isSameSelectCustomFieldValue(def issue, def fieldId, def value) {
        issue.getCustomFieldValue(ComponentAccessor.getCustomFieldManager().getCustomFieldObject(fieldId)).getValue()
                .equals(value)
    }

    static List<Option> getOptions(Issue issue, CustomField customField, List<String> optionList) {
        def fieldConfig = customField.getRelevantConfig(issue)
        def options = ComponentAccessor.getOptionsManager().getOptions(fieldConfig)
        return options.findAll { it.value in optionList }
    }
}