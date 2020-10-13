import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.link.IssueLink
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.workflow.TransitionOptions

final Long projectId = 14000L
final Integer actionId = 51
final String issueTypeId = "12300"
final String statusId = "10705"

IssueLinkManager issueLinkManager = ComponentAccessor.getIssueLinkManager()
Long currentIssueId = issue.getId() //ComponentAccessor.getIssueManager().getIssueObject("-301")
ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
List<IssueLink> currentInwardLinks = issueLinkManager.getInwardLinks(currentIssueId)
IssueService issueService = ComponentAccessor.getIssueService()
IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
issueInputParameters.skipScreenCheck = true
TransitionOptions transitionOptions = new TransitionOptions(false, true, false, false)

if (isNotNull(currentUser, currentInwardLinks, issueService, issueInputParameters)) {
    for (IssueLink currentInwardLink : currentInwardLinks) {
        Issue issueForTransition = currentInwardLink.getSourceObject()
        if (isSuitableIssue(issueForTransition, projectId, issueTypeId, statusId)) {
            if (isComplitedExcept(issueLinkManager.getOutwardLinks(issueForTransition.getId()), currentIssueId)) {
                IssueService.TransitionValidationResult validationResult =
                        issueService.validateTransition(currentUser, issueForTransition.getId(),
                                actionId, issueInputParameters, transitionOptions)
                if (validationResult.isValid()) {
                    IssueService.IssueResult result = issueService.transition(currentUser, validationResult)
                    if (result.isValid()) {

                    }
                }
            }
        }
    }
}

def boolean isSuitableIssue(Issue checkingIssue, Long suitableProjectId, String suitableIssueTypeId, String suitableStatusId) {
    if (checkingIssue.getProjectId().equals(suitableProjectId)) {
        if (checkingIssue.getIssueTypeId().isCase(suitableIssueTypeId)) {
            if (checkingIssue.getStatusId().isCase(suitableStatusId)) {
                return true
            }
        }
    }
    return false
}

def boolean isComplitedExcept(List<IssueLink> currentOutwardLinks, Long issueId) {
    for (IssueLink link : currentOutwardLinks) {
        Issue i = link.getDestinationObject()
        if (!i.getId().equals(issueId)) {
            if (i.getResolution() == null) {
                return false
            }
        }
    }
    return true
}

def boolean isNotNull(Object... objects) {
    for (Object o : objects) {
        if (o == null) {
            return false
        }
    }
    return true
}
