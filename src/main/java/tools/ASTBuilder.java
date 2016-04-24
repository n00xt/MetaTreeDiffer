package tools;

import grammars.java.Java8BaseListener;
import grammars.java.Java8Lexer;
import grammars.java.Java8Parser;
import models.AST;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public final class ASTBuilder {

    public static String serializeAST(AST ast){
        ANTLRFileStream inputFile = null;
        try {
            inputFile = new ANTLRFileStream(ast.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Java8Lexer lexer = new Java8Lexer(inputFile);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        Java8BaseListener listener = new Java8BaseListener();
        walker.walk(listener, tree);
        return listener.getFamixRted();
    }

}
