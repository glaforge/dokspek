package dokspek

import org.junit.Test
import org.junit.runner.JUnitCore
import org.junit.BeforeClass
import org.junit.AfterClass
import groovy.util.logging.Log

/**
 * 
 * @author Guillaume Laforge
 */
@Log
class SampleSpecDocRunnerTest {
    private static testClass

    @BeforeClass
    static void createTest() {
        log.info "Creating test class using the DokspekRunner"

        testClass = new GroovyShell().evaluate """
            import org.junit.runner.RunWith
            import dokspek.junit.DokspekRunner

            @RunWith(DokspekRunner)
            class RunSpecifications { }

            RunSpecifications
        """
    }

    @Test
    void verifySuccessAndFailures() {
        def result = JUnitCore.runClasses(testClass)
        
        def descriptions = result.failures*.description*.displayName

        assert descriptions.contains("list-sum(SampleOne)")
        assert descriptions.contains("power-assert(SampleOne)")
        assert descriptions.contains("different-exception(SampleOne)")

        assert !descriptions.contains("assert-true(SampleOne)")
        assert !descriptions.contains("not-to-be-run(SampleOne)")
        assert !descriptions.contains("should-be-run(SampleOne)")
        assert !descriptions.contains("hidden-snippet(SampleOne)")
        assert !descriptions.contains("an-exception(SampleOne)")
        assert !descriptions.contains("not-compilable(SampleOne)")
        assert !descriptions.contains("person-class(SampleOne)")
        assert !descriptions.contains("person-instance\"(SampleOne)")
        assert !descriptions.contains("person-checks(SampleOne)")

        log.info "Expected tests failed properly"
    }

    @AfterClass
    static void checkRenderedOutput() {
        def config = ConfigurationHolder.fromClass(testClass)
        def report = new File(new File(config.outputDirectory), 'Sample One.html').text

        assert report.contains('groovy.lang.MissingMethodException: No signature of method: java.util.ArrayList.sump()')
        assert report.contains('assert listOne.findAll { it % 2 } == expected')
        assert report.contains('java.lang.UnsupportedOperationException')
        assert !report.contains('System.currentTimeMillis()')
        
        log.info "Basic rendered output checks done"
    }
}
