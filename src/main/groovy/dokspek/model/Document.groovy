package dokspek.model

import groovy.transform.TupleConstructor
import groovy.transform.CompileStatic

/**
 * 
 * @author Guillaume Laforge
 */
@TupleConstructor
@CompileStatic
class Document {
    String title
    String content
    
    Document previous
    Document next
    
    String toString() { "Spec: ${title}" }
}
