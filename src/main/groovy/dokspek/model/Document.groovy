package dokspek.model

import groovy.transform.TupleConstructor

/**
 * 
 * @author Guillaume Laforge
 */
@TupleConstructor
class Document {
    String title
    String content
    
    String toString() { "Spec: ${title}" }
}
