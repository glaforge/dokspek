package dokspek

import dokspek.junit.DokspekRunner
import org.junit.runner.RunWith

/**
 * 
 * @author Guillaume Laforge
 */
@RunWith(DokspekRunner)
@Configuration(
    specificationDirectory = 'src/main/resources/dokspek',
    outputDirectory = 'build/documentation',
    assetsDirectory = "src/test/resources/dokspek/assets",
    templateDirectory = "src/test/resources/dokspek/templates"
)
class TestAndProduceDocumentation { }
