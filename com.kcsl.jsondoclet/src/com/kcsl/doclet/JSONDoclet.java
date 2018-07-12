package com.kcsl.doclet;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import javax.json.Json;
import javax.json.JsonWriter;

import org.apache.commons.io.FileUtils;

import com.sun.javadoc.*;

public final class JSONDoclet {
	
	private static final String OUTPUT_DIRECTORY_LOCATION = "/Users/ahmedtamrawi/Desktop/test/java-docs/";
	
	private static Path OUTPUT_DIRECTORY_PATH;

    public static boolean start(RootDoc rootDoc) {
    	prepareOutputDirectoryStructure();
    	
    	StringWriter stringWriter = new StringWriter();
    	 JsonWriter jsonWriter = Json.createWriter(stringWriter);
    	 jsonWriter.writeObject(Json.createObjectBuilder().build());
    	 jsonWriter.close();
        File file = new File("/Users/ahmedtamrawi/Desktop/test/doclet-methodlist.csv");
        try {
            PrintWriter out = new PrintWriter(file);
            try {
                writeTo(out, rootDoc);
                if (out.checkError())
                    return false;
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }
    
    private static void prepareOutputDirectoryStructure() {
    	File outputDirectoryFile = new File(OUTPUT_DIRECTORY_LOCATION);
    	if(outputDirectoryFile.exists()) {
    		try {
				FileUtils.cleanDirectory(outputDirectoryFile);
			} catch (IOException e) {
				System.err.println("Error cleaning the output directory: " + OUTPUT_DIRECTORY_LOCATION);
			}
    	}else {
    		outputDirectoryFile.mkdirs();
    	}
    	OUTPUT_DIRECTORY_PATH = outputDirectoryFile.toPath();
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    private static void writeTo(PrintWriter out, RootDoc rootDoc) {
        // header
        out.println(join(new String[]{"class", "package", "FQCN", "method", "modifiers",
                                      "synthetic?", "parameters", "return type", "since"}));
        // body
        for (ClassDoc classDoc : rootDoc.classes()) {
        	if(!classDoc.name().equals("XSSFWorkbook")) {
        		continue;
        	}
            String[] a = new String[12];
            Arrays.fill(a, "");
            int i = -1;
            a[++i] = classDoc.name();
            a[++i] = classDoc.containingPackage().name();
            a[++i] = classDoc.qualifiedName();
            a[++i] = classDoc.getRawCommentText();
            a[++i] = "";
            a[++i] = "";
            a[++i] = "";
            a[++i] = "";
            a[++i] = "";
            out.println(join(a));
        }
    }

    private static String parseSince(Doc... docs) {
        for (Doc doc : docs) {
            String s = join(doc.tags("since")).replace(",", "");
            if (s.contains("@since:"))
                s = s.replaceAll("@since:", "");
            s = s.trim();
            if (!s.isEmpty())
                return s;
        }
        return "";
    }

    private static String quote(Object o) {
        return String.format("\"%s\"", o);
    }

    private static <T> String join(T[] a) {
        if (a.length == 0)
            return "";
        StringBuilder s = new StringBuilder(String.valueOf(a[0]));
        for (int i = 1; i < a.length; i++)
            s.append(",").append(a[i]);
        return s.toString();
    }

}