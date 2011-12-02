package dokspek.junit

import dokspek.ConfigurationHolder
import dokspek.DocumentCollector
import dokspek.Utilities
import dokspek.model.Document
import groovy.text.SimpleTemplateEngine
import java.lang.annotation.Annotation
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.xwiki.component.embed.EmbeddableComponentManager
import org.xwiki.rendering.block.Block
import org.xwiki.rendering.block.MacroBlock
import org.xwiki.rendering.block.RawBlock
import org.xwiki.rendering.block.XDOM
import org.xwiki.rendering.block.match.MacroBlockMatcher
import org.xwiki.rendering.parser.Parser
import org.xwiki.rendering.renderer.BlockRenderer
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter
import org.xwiki.rendering.syntax.Syntax
import org.xwiki.rendering.transformation.Transformation
import org.xwiki.rendering.transformation.TransformationContext
import groovy.io.FileType

/**
 *
 * @author Guillaume Laforge
 */
class DokspekRunner extends Runner {

    ConfigurationHolder configuration
    EmbeddableComponentManager componentManager
    GroovyShell shell

    DokspekRunner(Class testClass) {
        super()

        this.configuration = ConfigurationHolder.fromClass(testClass)

        this.componentManager = new EmbeddableComponentManager()
        componentManager.initialize(this.class.classLoader)

        this.shell = new GroovyShell()
    }

    Description getDescription() {
        return Description.createSuiteDescription("Dokspek", new Annotation[0])
    }

    void run(RunNotifier notifier) {
        List<Document> specDocs = DocumentCollector.collect(configuration)
        specDocs.each { Document document ->
            // parse document, execute tests, render output

            def parser = componentManager.lookup(Parser, Syntax.XWIKI_2_0.toIdString())
            def xdom = parser.parse(new StringReader(document.content))

            List<MacroBlock> allGroovySnippets = xdom.getBlocks(new MacroBlockMatcher("test"), Block.Axes.DESCENDANT)
            allGroovySnippets.each { MacroBlock mb ->
                def description = Description.createTestDescription(Utilities.customClassName(document.title), mb.getParameter('name'))
                try {
                    notifier.fireTestStarted(description)

                    shell.evaluate(mb.content, mb.getParameter('name'))

                    notifier.fireTestFinished(description)
                } catch (Throwable t) {
                    def failureStacktraceBlock =
                        new RawBlock("<div class='stacktrace-message'>${Utilities.formatCleanTrace(t)}</div>", Syntax.XHTML_1_0)
                    mb.parent.insertChildAfter(failureStacktraceBlock, mb)
                    
                    notifier.fireTestFailure(new Failure(description, t))
                }
            }

            renderAndOutputReport(xdom, document)
        }

        copyAssets()
    }

    protected void copyAssets() {
        def assetsDir = new File(configuration.assetsDirectory)
        assert assetsDir.exists(), "The assets directory could not be found"

        def outputDir = new File(configuration.outputDirectory)
        if (!outputDir.exists()) outputDir.mkdir()

        assetsDir.eachFile(FileType.FILES) { File f ->
            f.withInputStream { InputStream ins ->
                def outputAssetFile = new File(outputDir, f.name)
                outputAssetFile.createNewFile()
                outputAssetFile << ins
            }
        }
    }

    protected void renderAndOutputReport(XDOM xdom, Document document) {
        def transform = componentManager.lookup(Transformation, "macro")
        def xformContext = new TransformationContext(xdom, Syntax.XWIKI_2_0)
        transform.transform(xdom, xformContext)

        def printer = new DefaultWikiPrinter()
        def renderer = componentManager.lookup(BlockRenderer, Syntax.XHTML_1_0.toIdString())
        renderer.render(xdom, printer);

        // merge rendered output into the templates

        def mainTemplateFile = new File(configuration.templateDirectory, "main.html")
        assert mainTemplateFile.exists(), "Main template file not found"

        def engine = new SimpleTemplateEngine(false)
        def template = engine.createTemplate(mainTemplateFile)

        def outputDirectory = new File(configuration.outputDirectory)
        if (!outputDirectory.exists()) outputDirectory.mkdirs()

        new File(outputDirectory, document.title + '.html').withWriter('UTF-8') { Writer writer ->
            writer << template.make([title: document.title, content: printer.toString()])
        }
    }
}
