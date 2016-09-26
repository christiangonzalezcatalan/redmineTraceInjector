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
    RestBuilder restClient = new RestBuilder()
    String redmineUrl = Holders.grailsApplication.config.getProperty('injector.redmineUrl')
    String gemsbbUrl = Holders.grailsApplication.config.getProperty('injector.gemsbbUrl')

    private def getMemberByEmail(redmineUserId) {
        def resp = restClient.get(
            "${redmineUrl}/users/${redmineUserId}.json?key=baa9da1d47247ea95bedc425027e7bb30df8f883")

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
        println "${gemsbbUrl}/projects/${projectId}/mappings?tool=${tool}"
        println "mapping: ${resp.json}"

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

    private def getTimeEntriesFromRedmine(redmineProjectId) {
        def resp =  restClient.get("${redmineUrl}/time_entries.json?project_id=${redmineProjectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener los registros de hora de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json.time_entries
    }

    private def getRedmineTaskById(Integer id) {
        def resp = restClient.get("${redmineUrl}/issues/${id}.json")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener la tarea de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json.issue
    }

    private def buildDetail(timeEntry) {
        [ member: [id: getMemberByEmail(timeEntry.user.id).id],
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

    def injectProjectTrace(String projectId, String externalProjectId) {
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
        def redmineTimeEntries = getTimeEntriesFromRedmine(externalProjectId)

        if(redmineTimeEntries.size() > 0) {
            def taskTraceMap = new LinkedHashMap()
            redmineTimeEntries.each {
                if(!taskTraceMap.containsKey(it.issue.id)) {
                    taskTraceMap[it.issue.id] = [name: getRedmineTaskById(it.issue.id).subject, details: []]
                }
                taskTraceMap[it.issue.id].details.add(buildDetail(it))
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

            println trace
            println mapping
            def bbTrace = saveBlackboardTrace(trace, projectId, taskTraceList)
            def bbMapping = saveBlackboardMapping(mapping, projectId, bbTrace)
        }
    }
}
