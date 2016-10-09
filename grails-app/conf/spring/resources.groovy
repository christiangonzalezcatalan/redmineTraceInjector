// Place your Spring DSL code here
beans = {
    injectorService(redmineTraceInjector.InjectorService) {}
    injectorJob(redmineTraceInjector.InjectorJob) {
        injectorService = ref("injectorService")
        //logService = ref("logService")
    }
}
