package dokspek

import org.junit.Test

/**
 * 
 * @author Guillaume Laforge
 */
@Configuration(
    outputDirectory = 'output',
    assetsDirectory = 'assets',
    specificationDirectory = 'specifications',
    templateDirectory = 'templates'
)
class ConfigurationAnnotationTest {

    @Test
    void verifyConfigurationValues() {
        def anno = ConfigurationHolder.fromClass(this.class)

        assert anno.outputDirectory == 'output'
        assert anno.assetsDirectory == 'assets'
        assert anno.specificationDirectory == 'specifications'
        assert anno.templateDirectory == 'templates'
    }
}
