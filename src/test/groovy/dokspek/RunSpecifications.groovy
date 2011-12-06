package dokspek

import org.junit.runner.RunWith
import dokspek.junit.DokspekRunner
import org.junit.AfterClass

/**
 * 
 * @author Guillaume Laforge
 */
@RunWith(DokspekRunner)
class RunSpecifications {
    
    @AfterClass void checkRenderedOutput() {
        def config = ConfigurationHolder.fromClass(this.class)
        def report = new File(new File(config.outputDirectory), 'Sample One.html').text

        assert report.contains('groovy.lang.MissingMethodException: No signature of method: java.util.ArrayList.sump()')
        assert report.contains('assert listOne.findAll { it % 2 } == expected')
        assert report.contains('java.lang.UnsupportedOperationException')
        assert !report.contains('System.currentTimeMillis()')
    }
}
