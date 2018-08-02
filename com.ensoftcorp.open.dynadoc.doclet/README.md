# DynaDoc Doclet

This project contains a custom Java Doc Doclet, that will process a project and outputs a list of JSON files for each class in the processed project.

Use the following command line in the directory containing the project to run the documentation generation:

`find . -type f -name "*.java" | xargs javadoc -private -doclet com.kcsl.doclet.JSONDoclet -docletpath "absolute path to doclet project bin folder" -output "the output directory for javadoc`


## Note on using of org.json.simple Project

We had to include the org.json.simple project from https://github.com/fangyidong/json-simple to make sure that the output directory includes all needed classes for plugin export. Exporting a normal java project along a plugin seems a tedious job and a lot of symbolic linking that is machine dependent and error-prone.

Refer to the https://github.com/fangyidong/json-simple for proper licensing and copyright information before using this project.

Authors for "org.json.simple" project: 
Yidong Fang
Chris Nokleberg
Dave Hughes

