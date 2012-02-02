package dokspek.wiki

import org.xwiki.rendering.macro.AbstractMacro
import org.xwiki.rendering.block.Block
import org.xwiki.rendering.transformation.MacroTransformationContext
import org.xwiki.component.annotation.Component
import javax.inject.Named
import org.xwiki.rendering.block.RawBlock
import org.xwiki.rendering.syntax.Syntax
//import groovy.transform.CompileStatic

/**
 * 
 * @author Guillaume Laforge
 */
@Component
@Named("test")
//@CompileStatic
class GroovyTestMacro extends AbstractMacro<GroovyTestMacroParameters> {

    GroovyTestMacro() {
        super("test", "Groovy Test Macro", GroovyTestMacroParameters)
    }

    boolean supportsInlineMode() {
        return true
    }

    List<Block> execute(GroovyTestMacroParameters params, String content, MacroTransformationContext context) {
        if (params.hidden == 'true') {
            return [new RawBlock("", Syntax.XHTML_1_0)]
        } else {
            return [new RawBlock("<pre><code class='groovy \${params.name}'>${content}</code></pre>", Syntax.XHTML_1_0)]
        }
    }
}
