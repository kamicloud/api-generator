package com.kamicloud.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("unused")
public class DocParser {
    static HashMap<String, String> classDocHashMap = new HashMap<>();


    public void parse(File file) throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(file);

        compilationUnit.getChildNodes().forEach(node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration template = (ClassOrInterfaceDeclaration) node;

                parseClassOrInterfaceDeclaration(template);
            }
            if (node instanceof EnumDeclaration) {
                EnumDeclaration error = (EnumDeclaration) node;

                parseEnumDeclaration(error);
            }
        });
    }

    public void parseClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        Optional<String> classpath = classOrInterfaceDeclaration.getFullyQualifiedName();

        Optional<Comment> classComment = classOrInterfaceDeclaration.getComment();

        if (classpath.isPresent() && classComment.isPresent()) {
            classDocHashMap.put(classpath.get(), parseComment(classComment.get()));
        }

        classOrInterfaceDeclaration.getMembers().forEach(bodyDeclaration -> {
            bodyDeclaration.ifEnumDeclaration(this::parseEnumDeclaration);
            bodyDeclaration.ifClassOrInterfaceDeclaration(this::parseClassOrInterfaceDeclaration);
            bodyDeclaration.ifFieldDeclaration(fieldDeclaration -> {
                Optional<Comment> comment = fieldDeclaration.getComment();
                fieldDeclaration.getVariables().forEach(variableDeclarator -> {
                    String name = variableDeclarator.getNameAsString();
                    if (classpath.isPresent() && comment.isPresent()) {

                        classDocHashMap.put(classpath.get() + "." + name, parseComment(comment.get()));
                    }
                });
            });
        });
    }

    public String parseComment(Comment comment) {
        String string;

        if (comment.isJavadocComment()) {
            Javadoc javadoc = comment.asJavadocComment().parse();
            string = javadoc.getDescription().toText();
        } else {
            string = comment.getContent().trim();
        }

        return string;
    }

    public void parseEnumDeclaration(EnumDeclaration enumDeclaration) {
        Optional<Comment> enumComment = enumDeclaration.getComment();
        String classpath;
        if (!enumDeclaration.getFullyQualifiedName().isPresent()) {
            return;
        }

        classpath = enumDeclaration.getFullyQualifiedName().get();
        enumComment.ifPresent(comment -> classDocHashMap.put(classpath, parseComment(comment)));
        enumDeclaration.getEntries().forEach(enumConstantDeclaration -> {
            String name = enumConstantDeclaration.getNameAsString();
            Optional<Comment> comment = enumConstantDeclaration.getComment();

            if (!comment.isPresent()) {
                return;
            }
            classDocHashMap.put(classpath + "." + name, parseComment(comment.get()));
        });
    }

    public void parseEnumConstantDeclaration(EnumConstantDeclaration enumConstantDeclaration) {
        Optional<Node> node = enumConstantDeclaration.getParentNode();

        node.ifPresent(node1 -> {

        });

    }
}
