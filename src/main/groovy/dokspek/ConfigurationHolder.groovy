package dokspek

import groovy.transform.TupleConstructor

/**
 * 
 * @author Guillaume Laforge
 */
@TupleConstructor
class ConfigurationHolder {
    String specificationDirectory   = "src/test/resources/dokspek/specifications"
    String assetsDirectory          = "src/test/resources/dokspek/assets"
    String templateDirectory        = "src/test/resources/dokspek/templates"
    String outputDirectory          = "build/reports/dokspek"

    static ConfigurationHolder fromClass(Class clazz) {



        return new ConfigurationHolder()
    }
}
