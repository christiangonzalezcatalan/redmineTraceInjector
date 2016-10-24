/*package redmineTraceInjector

class InjectorJob {
    static triggers = {
        println "configuración de triggers"
        if(1 > 0) {
            simple repeatInterval: 20000l
        }
    }

    def injectorService

    def execute(context) {
        println "se ejecuta: ${context.mergedJobDataMap}"
        println this
        injectorService.injectProcess()
    }
}
*/