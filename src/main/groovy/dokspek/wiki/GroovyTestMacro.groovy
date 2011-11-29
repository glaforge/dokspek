package dokspek.wiki

import org.xwiki.rendering.macro.AbstractMacro
import org.xwiki.rendering.block.Block
import org.xwiki.rendering.transformation.MacroTransformationContext
import org.xwiki.component.annotation.Component
import javax.inject.Named
import org.xwiki.rendering.block.WordBlock

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
        return [new WordBlock("test")]
    }
}
