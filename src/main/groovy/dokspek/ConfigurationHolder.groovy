package dokspek

import groovy.transform.TupleConstructor

/**
 * 
 * @author Guillaume Laforge
 */
@TupleConstructor
class ConfigurationHolder {
    String specificationDirectory   = "src/test/resources/specifications"
    String templateDirectory        = "src/main/resources/dokspek/templates"
    String outputDirectory          = "build/reports/dokspek"

    static ConfigurationHolder fromClass(Class clazz) {



        return new ConfigurationHolder()
    }
}
