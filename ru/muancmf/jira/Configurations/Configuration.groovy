package ru.muancmf.jira.Configurations

import ru.muancmf.jira.Operations.Operations

class Configuration {
    static def externalId = "customfield_10010"
    static def productLine = "customfield_10032"
    static Map<String, Map<String, Closure>> fields = [
            summary    : [setValue: { issue, value -> Operations.setSummary(issue, value) },
                          isSame  : { issue, value -> Operations.isSameSummary(issue, value) }],
            description: [setValue: { issue, value -> Operations.setDescription(issue, value) },
                          isSame  : { issue, value -> Operations.isSameDescription(issue, value) }],
            externalId : [setValue: { issue, value -> Operations.setTextCustomFieldValue(issue, externalId, value) },
                          isSame  : { issue, value -> Operations.isSameTextCustomFieldValue(issue, externalId, value) }],
            productLine: [setValue: { issue, value -> Operations.setSelectCustomFieldValue(issue, productLine, value) },
                          isSame  : { issue, value -> Operations.isSameSelectCustomFieldValue(issue, productLine, value) }]
    ]
}
