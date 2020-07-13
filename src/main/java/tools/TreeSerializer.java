package tools;

import grammars.java.Java8BaseListener;
import grammars.java.Java8Lexer;
import grammars.java.Java8Parser;
import helpers.MongoDB;
import models.AST;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class TreeSerializer {
    private List<AST> serializedTrees;
    private long startTime;
    private long finishTime;

    public TreeSerializer(List<AST> astList) {
        startTime = new Date().getTime();
        System.out.println("\nTree serializing started...\n");
        this.serializedTrees = astList;
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        parseAndSaveTree(ds);

    }

    private void parseAndSaveTree(Datastore ds){
        for (AST ast: serializedTrees){
            System.out.print(".");
            try {
                Query<AST> query = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(ast.getId());
                ANTLRFileStream inputFile = new ANTLRFileStream(ast.getFilename());
                Java8Lexer lexer = new Java8Lexer(inputFile);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                Java8Parser parser = new Java8Parser(tokens);
                ParseTree tree = parser.compilationUnit();
                ParseTreeWalker walker = new ParseTreeWalker();
                Java8BaseListener listener = new Java8BaseListener();
                walker.walk(listener, tree);
                ast.setFAMIXrtedTree(listener.getFamixRted());
                UpdateOperations<AST> ops = ds.createUpdateOperations(AST.class).set("FAMIXrtedTree", listener.getFamixRted());
                ds.update(query, ops);
            } catch (IOException e) {
                System.out.println("Cannot create AST, file read error !");
            }
        }
        finishTime = new Date().getTime();
        System.out.println("\nSerializing finished - duration: "+(finishTime - startTime)/1000.0+" s\n");
    }

    public List<AST> getSerializedTrees() {
        return serializedTrees;
    }
}
