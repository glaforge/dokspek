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
import org.xwiki.rendering.converter.Converter
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter
import org.xwiki.rendering.syntax.Syntax
import groovy.text.SimpleTemplateEngine

/**
 *
 * @author Guillaume Laforge
 */
class DokspekRunner extends Runner {

    ConfigurationHolder configuration
    Converter converter

    DokspekRunner(Class testClass) {
        super()

        this.configuration = ConfigurationHolder.fromClass(testClass)

        def componentManager = new EmbeddableComponentManager()
        componentManager.initialize(this.class.classLoader)
        converter = componentManager.lookup(Converter)
    }

    Description getDescription() {
        def mainDescription = Description.createSuiteDescription("Dokspek", new Annotation[0])

        return mainDescription
    }
    
    private static Class customClassName(String label) {
        String replacement = label.replaceAll('[^a-zA-Z0-9]', '')
        Eval.me("class ${replacement} {}; ${replacement}")
    }
    
    void run(RunNotifier notifier) {
        List<Document> specDocs = DocumentCollector.collect(configuration)
        specDocs.each { Document specDoc -> 
            def description = Description.createTestDescription(customClassName(specDoc.title), specDoc.title)
            try {
                notifier.fireTestStarted(description)

                runSpecAndReport(specDoc)

                notifier.fireTestFinished(description)
            } catch (Throwable t) {
                notifier.fireTestFailure(new Failure(description, t))
            }
        }
    }

    void runSpecAndReport(Document document) {
        def printer = new DefaultWikiPrinter()
        converter.convert(new StringReader(document.content), Syntax.XWIKI_2_0, Syntax.XHTML_1_0, printer)

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
