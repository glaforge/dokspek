package dokspek

import groovy.transform.TupleConstructor
import groovy.transform.CompileStatic

/**
 * 
 * @author Guillaume Laforge
 */
@TupleConstructor
@CompileStatic
class ConfigurationHolder {
    String specificationDirectory   = "src/test/resources/dokspek/specifications"
    String assetsDirectory          = "src/test/resources/dokspek/assets"
    String templateDirectory        = "src/test/resources/dokspek/templates"
    String outputDirectory          = "build/reports/dokspek"

    static ConfigurationHolder fromClass(Class clazz) {
        def holder = new ConfigurationHolder()

        Configuration anno = clazz.getAnnotation(Configuration)
        
        if (anno) {
            if (anno.specificationDirectory())
                holder.specificationDirectory = anno.specificationDirectory()
            if (anno.assetsDirectory())
                holder.assetsDirectory = anno.assetsDirectory()
            if (anno.templateDirectory())
                holder.templateDirectory = anno.templateDirectory()
            if (anno.outputDirectory())
                holder.outputDirectory = anno.outputDirectory()
        }

        return holder
    }
}
