# JSON Doclet

This project contains a custom Java Doc Doclet, that will process a project and outputs a list of JSON files for each class in the process project.

Make sure to copy the contents of the jar files into the bin directory so that the JavaDoc tool can find the dependent jars.

Use the following command line in the directory containing the project to run the documentation generation:

`find . -type f -name "*.java" | xargs javadoc -private -doclet com.kcsl.doclet.JSONDoclet -docletpath "absolute path to doclet project bin folder"`
