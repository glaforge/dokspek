package dokspek

import dokspek.model.Document
import groovy.io.FileType

/**
 * 
 * @author Guillaume Laforge
 */
class DocumentCollector {

    static List<Document> collect(ConfigurationHolder config) {
        File specificationDirectory = new File(config.specificationDirectory)
        assert specificationDirectory.exists(), "The directory for your specifications doesn't exist (${specificationDirectory})"

        def tocFile = new File(specificationDirectory, "toc.txt")

        List<Document> docs = []

        // if there's a table of content indicating the spec files and their order, use that
        // otherwise, search all the spec files in that directory
        if (tocFile.exists()) {
            tocFile.eachLine { String fileName ->
                def spec = new File(specificationDirectory, fileName)
                assert spec.exists(), "The specification ${fileName} couldn't be found"

                docs << new Document(fileNameWithoutExtension(spec.name), spec.getText('UTF-8'))
            }
        } else {
            specificationDirectory.eachFileRecurse(FileType.FILES) { File spec ->
                docs << new Document(fileNameWithoutExtension(spec.name), spec.getText('UTF-8'))
            }
        }
        
        return docs
    }

    static private String fileNameWithoutExtension(String fileName) {
        int lastSlash = Math.max(fileName.lastIndexOf('/'), 0)
        int lastDot = fileName.lastIndexOf('.')

        return fileName[lastSlash..<(lastDot > 0 ? lastDot : fileName.length())]
    }
}
