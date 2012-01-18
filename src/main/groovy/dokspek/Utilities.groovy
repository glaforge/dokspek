package dokspek

import groovy.transform.CompileStatic

/**
 *
 * @author Guillaume Laforge
 */
@CompileStatic
class Utilities {
    private static Map CLASS_CACHE = [:].withDefault { String label ->
        String replacement = label.replaceAll('[^a-zA-Z0-9]', '')
        Eval.me("class ${replacement} {}; ${replacement}")
    }

    static Class customClassName(String label) {
        CLASS_CACHE[label]
    }

    private static void sanitizeStacktrace(Throwable t) {
        def filtered = [
                'java.', 'javax.', 'sun.', 'groovy.', 'org.codehaus.groovy.',
                'dokspek.', 'org.gradle.', 'com.intellij.', 'org.junit.', '$Proxy'
        ]
        def trace = t.stackTrace
        def newTrace = []
        trace.each { StackTraceElement stackTraceElement ->
            if (filtered.every { String it -> !stackTraceElement.className.startsWith(it) }) {
                newTrace << stackTraceElement
            }
        }

        def clean = (StackTraceElement[]) newTrace.toArray(newTrace as StackTraceElement[])
        t.stackTrace = clean
    }

    static void deepSanitize(Throwable t) {
        Throwable current = t
        while (current.cause != null) {
            current = current.cause
            sanitizeStacktrace(current)
        }
        sanitizeStacktrace(t);
    }
}
