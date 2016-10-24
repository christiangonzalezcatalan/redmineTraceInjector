// Place your Spring DSL code here
beans = {
    injectorService(redmineTraceInjector.InjectorService) {}
    injectorConsumer(redmineTraceInjector.InjectorConsumer) {
        injectorService = ref("injectorService")
    }
}
