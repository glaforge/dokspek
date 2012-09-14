package dokspek.junit

import dokspek.ConfigurationHolder
import dokspek.DocumentCollector
import dokspek.Utilities
import dokspek.model.Document
import groovy.text.SimpleTemplateEngine
import org.junit.runner.Description
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
import org.codehaus.groovy.control.CompilationFailedException
import java.lang.reflect.Method
import org.junit.BeforeClass
import org.junit.AfterClass
import org.xwiki.rendering.block.match.ClassBlockMatcher
import org.xwiki.rendering.block.HeaderBlock
import org.junit.runners.ParentRunner
import groovy.transform.CompileStatic

/**
 *
 * @author Guillaume Laforge
 */
// TODO @CompileStatic
class DokspekRunner extends ParentRunner<Document> {

    protected final ConfigurationHolder configuration
    protected GroovyShell shell
    private EmbeddableComponentManager componentManager
    private Class testClass
    @Lazy List<Document> documents = DocumentCollector.collect(configuration)

    DokspekRunner(Class testClass) {
        super(testClass)
        
        this.testClass = testClass

        this.configuration = ConfigurationHolder.fromClass(testClass)

        this.componentManager = new EmbeddableComponentManager()
        componentManager.initialize(this.class.classLoader)

        this.shell = new GroovyShell()
    }

    @Override
    protected List<Document> getChildren() {
        return documents
    }

    @Override
    protected Description describeChild(Document document) {
        def description = Description.createSuiteDescription(Utilities.customClassName(document.title))
        return description
    }

    @Override
    protected void runChild(Document document, RunNotifier notifier) {
        // parse document, execute tests, render output

        def parser = componentManager.lookup(Parser, Syntax.XWIKI_2_0.toIdString())
        def xdom = parser.parse(new StringReader(document.content))

        List<MacroBlock> allScriptBlocks = xdom.getBlocks(new MacroBlockMatcher('test'), Block.Axes.DESCENDANT)
        List<HeaderBlock> allHeaderBlocks = xdom.getBlocks(new ClassBlockMatcher(HeaderBlock), Block.Axes.DESCENDANT)

        addScriptAnchors(allScriptBlocks)
        addHeaderAnchors(allHeaderBlocks)

        // keep a map of script names and script contents
        Map<String, String> scripts = [:]

        allScriptBlocks.each { MacroBlock mb ->
            scripts[mb.getParameter('name')] = mb.content

            // don't run the snippet as a test if marked with run="false"
            if (mb.getParameter('run') != "false") {
                def description = Description.createTestDescription(Utilities.customClassName(document.title), mb.getParameter('name'))
                try {
                    notifier.fireTestStarted(description)

                    try {
                        String scriptText = mb.content

                        // concatenate dependent scripts together to form one single script to execute
                        if (mb.getParameter('dependsOn')) {
                            def dependentScripts = mb.getParameter('dependsOn').split(',').collect { String it -> it.trim() }
                            def concatenatedScripts = dependentScripts.collect { String scriptName -> scripts[scriptName] } << scriptText
                            scriptText = concatenatedScripts.join('\n')
                        }

                        shell.evaluate(scriptText, mb.getParameter('name'))
                    } catch (CompilationFailedException cfe) {
                        // if snippet marked as "compiles=false", a compilation exception is expected
                        if (mb.getParameter('compiles') != 'false')
                            throw cfe
                    } catch (Throwable t) {
                        // if snippet marked as "throws", check that the right exception is thrown
                        if (t.class.name != mb.getParameter('throws'))
                            throw t
                    }
                } catch (Throwable t) {
                    notifier.fireTestFailure(new Failure(description, t))

                    Utilities.deepSanitize(t)

                    def sw = new StringWriter()
                    t.printStackTrace(new PrintWriter(sw))
                    def failureStacktraceBlock =
                        new RawBlock("<div class='stacktrace-message'>${sw}</div>", Syntax.XHTML_1_0)
                    mb.parent.insertChildAfter(failureStacktraceBlock, mb)
                } finally {
                    notifier.fireTestFinished(description)
                }
            }
        }

        renderAndOutputReport(xdom, document)
    }

    void run(RunNotifier notifier) {
        setupDirectory()

        runBeforeClass()

        super.run(notifier)

        runAfterClass()

        tableOfContents()

        copyAssets()
    }

    private void addHeaderAnchors(List<HeaderBlock> blocks) {
        blocks.each { HeaderBlock block ->
            block.parent.insertChildBefore(new RawBlock("<a name='${block.id}'></a>", Syntax.XHTML_1_0), block)
        }
    }

    private void addScriptAnchors(List<MacroBlock> blocks) {
        blocks.each { MacroBlock block ->
            block.parent.insertChildBefore(new RawBlock("<a name='${block.getParameter('name')}'></a>", Syntax.XHTML_1_0), block)
        }
    }

    protected void runBeforeClass() {
        def classInstance = testClass.newInstance()

        testClass.getMethods().findAll { Method method -> method.getAnnotation(BeforeClass) }
                .each { Method method -> method.invoke(classInstance) }
    }

    protected void runAfterClass() {
        def classInstance = testClass.newInstance()

        testClass.getMethods().findAll { Method method -> method.getAnnotation(AfterClass) }
                .each { Method method -> method.invoke(classInstance) }
    }
    
    protected void setupDirectory() {
        def outputDir = new File(configuration.outputDirectory)
        outputDir.deleteDir()
    }

    protected void tableOfContents() {
        def tocTemplateFile = new File(configuration.templateDirectory, "toc.html")
        assert tocTemplateFile.exists(), "Table of Content template file not found"

        def engine = new SimpleTemplateEngine(false)
        def template = engine.createTemplate(tocTemplateFile)

        def outputDirectory = new File(configuration.outputDirectory)

        new File(outputDirectory, 'index.html').withWriter('UTF-8') { Writer writer ->
            writer << template.make([docs: documents])
        }
    }

    protected void copyAssets() {
        def assetsDir = new File(configuration.assetsDirectory)
        assert assetsDir.exists(), "The assets directory could not be found"

        def outputDir = new File(configuration.outputDirectory)
        if (!outputDir.exists()) outputDir.mkdir()

        assetsDir.eachFile(FileType.FILES) { File f ->
            f.withInputStream { InputStream ins ->
                def outputAssetFile = new File(outputDir, f.name)
                if (outputAssetFile.exists()) outputAssetFile.delete()
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
            writer << template.make([
                    title: document.title,
                    content: printer.toString(),
                    previous: document.previous,
                    next: document.next
            ])
        }
    }
}
