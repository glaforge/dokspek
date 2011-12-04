package dokspek.wiki

import org.xwiki.properties.annotation.PropertyMandatory

/**
 * 
 * @author Guillaume Laforge
 */
class GroovyTestMacroParameters {

    @PropertyMandatory
    public String name

    public String run = "true"

    public String exception
}
