package dokspek.wiki

import org.xwiki.rendering.macro.AbstractMacro
import org.xwiki.rendering.block.Block
import org.xwiki.rendering.transformation.MacroTransformationContext
import org.xwiki.component.annotation.Component
import javax.inject.Named
import org.xwiki.rendering.block.WordBlock
import org.xwiki.rendering.block.VerbatimBlock
import org.xwiki.rendering.block.RawBlock
import org.xwiki.rendering.syntax.Syntax

/**
 * 
 * @author Guillaume Laforge
 */
@Component
@Named("test")
class GroovyTestMacro extends AbstractMacro<GroovyTestMacroParameters> {

    GroovyTestMacro() {
        super("test", "Groovy Test Macro", GroovyTestMacroParameters)
    }

    boolean supportsInlineMode() {
        return false
    }

    List<Block> execute(GroovyTestMacroParameters params, String content, MacroTransformationContext context) {
        return [new RawBlock("<pre><code class='groovy'>${content}</code></pre>", Syntax.XHTML_1_0)]
    }
}
