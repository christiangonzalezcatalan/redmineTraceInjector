package redmineTraceInjector

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

import static redmineTraceInjector.Mocks.BlackboardResponses.getToolConfigurationFromBlackboard
import static redmineTraceInjector.Mocks.BlackboardResponses.getProjectFromBlackboard
import static redmineTraceInjector.Mocks.BlackboardResponses.getRepositoryFromBlackboard
import static redmineTraceInjector.Mocks.BlackboardResponses.getTraceFromBlackboard
import static redmineTraceInjector.Mocks.BlackboardResponses.getTraceMappingsFromBlackboard
import static redmineTraceInjector.Mocks.BlackboardResponses.getMemberByEmailFromBlackboard

import static redmineTraceInjector.Mocks.RedmineResponses.getTimeEntriesFromRedmine
import static redmineTraceInjector.Mocks.RedmineResponses.getUserFromRedmine
import static redmineTraceInjector.Mocks.RedmineResponses.listarRegistrosHorasTrabajadas
import static redmineTraceInjector.Mocks.RedmineResponses.getIssueFromRedmine

import grails.test.mixin.TestFor
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.Parameter
import org.mockserver.verify.VerificationTimes
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(InjectorService)
class InjectorServiceSpec extends Specification {

    protected static ClientAndServer mockServer

    def setupSpec() {
        mockServer = startClientAndServer(8081)
    }

    def cleanupSpec() {
        mockServer.stop()
    }

    def setup() {
    }

    def cleanup() {
        mockServer.reset()
    }

    void 'test inject process'() {
        setup:
        //def projectId = '57cc59368acec62bf2f7d7ed'
        //def redmineProjectId = '3'
        def projectId = '57ccad338acec633f77f862e'
        def redmineProjectId = '2'
        def redmineKey = 'baa9da1d47247ea95bedc425027e7bb30df8f883'

        mockServer.when(
                request('/toolsConfiguration')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('toolName', 'Redmine'))
                        .withQueryStringParameters(new Parameter('processName', 'RedmineTraceInjector'))
        ).respond(response(getToolConfigurationFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/projects/57ccad338acec633f77f862e')
                        .withMethod('GET')
        ).respond(response(getProjectFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request("/organizations/57e89b278acec6487695a4b5/repositories")
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('toolName', 'Redmine'))
        ).respond(response(getRepositoryFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/traces')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('projectId', projectId))
        ).respond(response(getTraceFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request("/projects/${projectId}/mappings")
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('tool', 'Redmine'))
        ).respond(response(getTraceMappingsFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/time_entries.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('project_id', redmineProjectId))
        ).respond(response(getTimeEntriesFromRedmine())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/issues/9.json')
                        .withMethod('GET')
        ).respond(response(getIssueFromRedmine(9, 'Carga de un plan de redmine en blackboard.'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/issues/11.json')
                        .withMethod('GET')
        ).respond(response(getIssueFromRedmine(9, 'Revisión de código.'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/users/3.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('key', redmineKey))
        ).respond(response(getUserFromRedmine(3))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/users/4.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('key', redmineKey))
        ).respond(response(getUserFromRedmine(4))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/members')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('email', 'christiangonzalezcatalan@hotmail.com'))
        ).respond(response(getMemberByEmailFromBlackboard("57c3c4858acec662dab6dcf4",
                            "christiangonzalezcatalan@hotmail.com",
                            "Christian González"))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
            request('/members')
                    .withMethod('GET')
                    .withQueryStringParameters(new Parameter('email', 'jperez@miempresita.cl'))
        ).respond(response(getMemberByEmailFromBlackboard("57c3c4838acec662dab6dcf2",
                            "jperez@miempresita.cl",
                            "Juan Pérez"))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
            request('/traces/57d5f5e48acec62fb22f8a73')
            .withMethod('PUT')
        ).respond(response(getTraceFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request("/projects/${projectId}/mappings/57d5f5e88acec62fb22f8a74")
                        .withMethod('PUT')
        ).respond(response(getTraceMappingsFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        expect:
        service.injectProcess()
    }
}

