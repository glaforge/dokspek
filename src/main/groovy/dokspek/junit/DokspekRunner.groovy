package dokspek.junit

import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.junit.runner.Description
import java.lang.annotation.Annotation
import dokspek.ConfigurationHolder
import dokspek.DocumentCollector
import dokspek.model.Document
import org.junit.runner.notification.Failure
import org.xwiki.component.embed.EmbeddableComponentManager

import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter
import org.xwiki.rendering.syntax.Syntax
import groovy.text.SimpleTemplateEngine
import org.xwiki.rendering.parser.Parser
import org.xwiki.rendering.renderer.BlockRenderer

import org.xwiki.rendering.transformation.Transformation
import org.xwiki.rendering.transformation.TransformationContext

import org.xwiki.rendering.block.MacroBlock
import org.xwiki.rendering.block.match.MacroBlockMatcher
import org.xwiki.rendering.block.Block
import org.xwiki.rendering.block.RawBlock

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
                def description = Description.createTestDescription(customClassName(document.title), mb.getParameter('name'))
                try {
                    notifier.fireTestStarted(description)

                    shell.evaluate(mb.content, mb.getParameter('name'))

                    notifier.fireTestFinished(description)
                } catch (Throwable t) {
                    def failureStacktraceBlock =
                        new RawBlock("<div class='stacktrace-message'>${formatCleanTrace(t)}</div>", Syntax.XHTML_1_0)
                    mb.parent.insertChildAfter(failureStacktraceBlock, mb)
                    
                    notifier.fireTestFailure(new Failure(description, t))
                }
            }

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

    private static Class customClassName(String label) {
        String replacement = label.replaceAll('[^a-zA-Z0-9]', '')
        Eval.me("class ${replacement} {}; ${replacement}")
    }

    private static void sanitizeStacktrace(Throwable t) {
        def filtered = [
            'java.', 'javax.', 'sun.', 'groovy.', 'org.codehaus.groovy.',
            'dokspek.', 'org.gradle.', 'com.intellij.', 'org.junit.', '$Proxy'
    	]
        def trace = t.stackTrace
        def newTrace = []
        trace.each { stackTraceElement ->
            if (filtered.every { !stackTraceElement.className.startsWith(it) }) {
                newTrace << stackTraceElement
            }
        }
        def clean = newTrace.toArray(newTrace as StackTraceElement[])
        t.stackTrace = clean
    }

    private static void deepSanitize(Throwable t) {
        Throwable current = t
        while (current.cause != null) {
            current = sanitizeStacktrace(current.cause)
        }
        sanitizeStacktrace(t);
    }

    private String formatCleanTrace(Throwable t) {
        deepSanitize(t)

        def stacktraceWriter = new StringWriter()
        t.printStackTrace(new PrintWriter(stacktraceWriter))
        String trace = stacktraceWriter.toString()
                .replaceAll('\n', '<br/>')
                .replaceAll(' ', '&nbsp;')
                .replaceAll('\t', '&nbsp;' * 4)
        return trace
    }
}
