package dokspek.wiki

import org.xwiki.properties.annotation.PropertyMandatory
import org.xwiki.properties.annotation.PropertyName

/**
 * 
 * @author Guillaume Laforge
 */
class GroovyTestMacroParameters {

    @PropertyMandatory
    public String name

    public String run = "true"

    @PropertyName("throws")
    public String exception
}
