# JavaDoc JSON Doclet

This project contains a custom Java Doc Doclet, that will process a project and outputs a list of JSON files for each class in the process project.

Make sure to copy the contents of the jar files into the bin directory so that the JavaDoc tool can find the dependent jars.

Use the following command line in the directory containing the project to run the documentation generation:

`find . -type f -name "*.java" | xargs javadoc -private -doclet com.kcsl.doclet.JSONDoclet -docletpath "absolute path to doclet project bin folder" -output "the output directory for javadoc`


## Use of org.json.simple Project

We had to include the org.json.simple project from [http://code.google.com/p/json-simple/] to make sure that the output directory includes all needed classes for plugin export. Exporting a normal java project along a plugin seems a tedious job and a lot of symbolic linking that is machine dependent and error-prone.

Refer to the [http://code.google.com/p/json-simple/] for proper licensing and copyright information before using this project.

Authors for "org.json.simple" project: 
Yidong Fang
Chris Nokleberg
Dave Hughes

