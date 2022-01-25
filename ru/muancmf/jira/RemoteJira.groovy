package ru.muancmf.jira

import groovyx.net.http.HTTPBuilder

class RemoteJira {
    private String host
    private String auth
    private List responses

    RemoteJira(String host, String auth) {
        this.host = host
        this.auth = auth
        this.responses = new ArrayList()
    }

    void fetchAllIssuesForJql(Map query) {
        new HTTPBuilder(host).get([path: "/rest/api/2/issue", contentType: "application/json", query: query]) { resp, reader ->
            assert resp.status == 200
            responses.add(reader)
            Optional.of(reader.startAt + reader.maxResults)
                    .filter(it -> it < reader.total)
                    .map(it -> fetchAllIssuesForJql(query.get("query").put("page", it)))
        }
    }

    def handleResponses(Closure responseHandler) {
        return responseHandler(responses)
    }
}

