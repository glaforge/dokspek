package dokspek

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType

/**
 * 
 * @author Guillaume Laforge
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface Configuration {
    String specificationDirectory()
    String assetsDirectory()
    String templateDirectory()
    String outputDirectory()
}
