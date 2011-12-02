package dokspek

/**
 * 
 * @author Guillaume Laforge
 */
@interface Configuration {
    String specificationDirectory() default ""
    String assetsDirectory()        default ""
    String templateDirectory()      default ""
    String outputDirectory()        default ""
}
