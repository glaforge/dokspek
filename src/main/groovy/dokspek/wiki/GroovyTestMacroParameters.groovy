package dokspek.wiki

import org.xwiki.properties.annotation.PropertyMandatory
import org.xwiki.properties.annotation.PropertyName
import groovy.transform.CompileStatic

/**
 * 
 * @author Guillaume Laforge
 */
@CompileStatic
class GroovyTestMacroParameters {

    @PropertyMandatory
    public String name

    public String run = "true"

    @PropertyName("throws")
    public String exception

    public String compiles = "true"

    public String dependsOn

    public String hidden = "false"
}
