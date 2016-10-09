package redmineTraceInjector

import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus
import grails.util.Holders
import org.bson.types.ObjectId

@Transactional
class InjectorService {
    private static String toolName = 'Redmine'
    private static String processName = 'RedmineTraceInjector'
    RestBuilder restClient = new RestBuilder()
    String gemsbbUrl = Holders.grailsApplication.config.getProperty('injector.gemsbbUrl')

    private def getToolsConfigurationFromBB() {
        def resp = restClient.get(
            "${gemsbbUrl}/toolsConfiguration?toolName=${InjectorService.toolName}&processName=${InjectorService.processName}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener la configuración del proceso ${InjectorService.processName}. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json
    }

    private def getRepositoryFromBB(organizationId) {
        def resp = restClient.get(
            "${gemsbbUrl}/organizations/${organizationId}/repositories?toolName=${InjectorService.toolName}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el repositorio. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    private def getProjectFromBB(String projectId) {
        def resp = restClient.get("${gemsbbUrl}/projects/${projectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro del plan del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        resp.json
    }

    private def getMemberByEmailFromRedmine(repository, redmineUserId) {
        def resp = restClient.get(
            "${repository.data.root}/users/${redmineUserId}.json?key=${repository.data.apiKey}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el usuario de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.user != null) {
            def memberResp = restClient.get(
                "${gemsbbUrl}/members?email=${result.user.mail}")
            memberResp.json
        }
    }

    private def getMappingFromBB(projectId, tool) {
        def resp = restClient.get(
            "${gemsbbUrl}/projects/${projectId}/mappings?tool=${tool}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el mapping del plan. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    private def getMapping(projectId, tool) {
        def mapping = getMappingFromBB(projectId, tool)

        if(mapping == null) {
            mapping = [
                project: [
                    id: projectId
                ],
                tool: tool,
                map: new ArrayList()
            ]
        }
        mapping
    }

    private def getTraceFromBB(String projectId) {
        def resp = restClient.get("${gemsbbUrl}/traces?projectId=${projectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro de traza del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    private def getTaskIdFromMap(id, map){
        def task = map.find() { it.externalId == id.toString() && it.entityType == 'Task' }

        if(task == null) {
            task = [
                internalId: new ObjectId().toString(),
                externalId: id.toString(),
                entityType: 'Task'
            ]
            map.add(task)
        }

        task.internalId
    }

    private def getTimeEntriesFromRedmine(repository, redmineProjectId) {
        def resp =  restClient.get("${repository.data.root}/time_entries.json?project_id=${redmineProjectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener los registros de hora de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json.time_entries
    }

    private def getRedmineTaskById(repository, Integer id) {
        def resp = restClient.get("${repository.data.root}/issues/${id}.json")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener la tarea de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json.issue
    }

    private def buildDetail(timeEntry, member) {
        [ member: [id: member.id],
          date: Date.parse('yyyy-MM-dd', timeEntry.spent_on),
          hours: timeEntry.hours]
    }

    private def saveBlackboardTrace(trace, projectId, taskTraceList) {
        def responseTrace
        if(trace.id == null) {
            responseTrace = restClient.post("${gemsbbUrl}/traces") {
                contentType "application/json"
                json (
                  project: [id: projectId],
                  taskTraces: taskTraceList
                )
            }
        }
        else {
            responseTrace = restClient.put("${gemsbbUrl}/traces/${trace.id}") {
                contentType "application/json"
                json (
                  id: trace.id,
                  project: [id: projectId],
                  taskTraces: taskTraceList
                )
            }
        }

        if (responseTrace.getStatusCode() != HttpStatus.OK &&
            responseTrace.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error al guardar el registro de la traza. HttpStatusCode: ${responseTrace.getStatusCode()}")
        }

        responseTrace.json
    }

    private def saveBlackboardMapping(mapping, projectId, bbObject) {
        def responseMapping

        if(mapping.id == null) {
            responseMapping = restClient.post("${gemsbbUrl}/projects/${projectId}/mappings") {
                contentType "application/json"
                json {
                    project = mapping.project
                    tool = mapping.tool
                    map = mapping.map
                }
            }
        }
        else {
            responseMapping = restClient.put("${gemsbbUrl}/projects/${projectId}/mappings/${mapping.id}") {
                contentType "application/json"
                json {
                    id = mapping.id
                    project = mapping.project
                    tool = mapping.tool
                    map = mapping.map
                }
            }
        }

        if (responseMapping.getStatusCode() != HttpStatus.OK &&
            responseMapping.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error al guardar el mapping del plan. HttpStatusCode: ${responseMapping.getStatusCode()}")
        }

        responseMapping.json
    }

    def injectProcess() {
        def toolsConfig = getToolsConfigurationFromBB()
        toolsConfig.each() {
            def project = getProjectFromBB(it.project.id)
            def repository = getRepositoryFromBB(project.organization.id)

            injectProjectTrace(it.project.id, it.parameters.projectId, repository)
        }
    }

    def injectProjectTrace(String projectId, Integer externalProjectId, repository) {
        def trace = getTraceFromBB(projectId)
        if(trace == null) {
            trace = [
                project: [
                    id: projectId
                ],
                taskTraces: new LinkedHashMap()
            ]
        }

        def mapping = getMapping(projectId, 'Redmine')
        def redmineTimeEntries = getTimeEntriesFromRedmine(repository, externalProjectId)

        if(redmineTimeEntries.size() > 0) {
            def taskTraceMap = new LinkedHashMap()
            redmineTimeEntries.each {
                if(!taskTraceMap.containsKey(it.issue.id)) {
                    taskTraceMap[it.issue.id] = [name: getRedmineTaskById(repository, it.issue.id).subject, details: []]
                }

                def member = getMemberByEmailFromRedmine(repository, it.user.id)
                taskTraceMap[it.issue.id].details.add(buildDetail(it, member))
            }

            def taskTraceList = taskTraceMap.collect {
                key, value ->
                    def taskId = getTaskIdFromMap(key, mapping.map)
                    def taskTrace = [taskTraceId: taskId]
                    taskTrace << [
                        name: value.name,
                        status: 'ESTADO!',
                        traceDetails: value.details
                    ]
            }

            def bbTrace = saveBlackboardTrace(trace, projectId, taskTraceList)
            def bbMapping = saveBlackboardMapping(mapping, projectId, bbTrace)
        }

        println "Traza del proyecto ${projectId} cargada."
    }
}
