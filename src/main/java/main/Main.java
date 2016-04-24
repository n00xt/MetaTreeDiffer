package main;

import helpers.MongoDB;
import models.AST;
import org.mongodb.morphia.Datastore;
import tools.ASTBuilder;
import tools.DistanceCalculator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static long time1;

    public static void main(String[] args) {
        time1 = new Date().getTime();
        List<AST> astFileList;
        try {
            for (int i = 0; i < args.length; i++) {
                astFileList = new ArrayList<>();
                listFiles(args[i], astFileList);
                saveFilesToDB(astFileList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listFiles(String directoryName, List<AST> astFileList) throws IOException {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                if (file.toString().endsWith(".java")){
                    String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                    astFileList.add(new AST(file.getAbsolutePath(), content.hashCode()));
                }
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), astFileList);
            }
        }
    }

    private static void saveFilesToDB(List<AST> astFileList) throws IOException {
        boolean calculate = false;
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        int noOfFiles = astFileList.size();
        for (int i = 0; i < astFileList.size(); i++) {
            if (ds.find(AST.class, "filename", astFileList.get(i).getFilename()).get() != null){
                Integer version = ds.find(AST.class, "filename", astFileList.get(i).getFilename()).order("-version").get().getVersion();
                if (ds.find(AST.class, "filename", astFileList.get(i).getFilename()).get().getHash() != astFileList.get(i).getHash()){
                    astFileList.get(i).setVersion(++version);
                    astFileList.get(i).setFAMIXrtedTree(ASTBuilder.serializeAST(astFileList.get(i)));
                    ds.save(astFileList.get(i));
                    System.out.println(noOfFiles-- +"\t"+astFileList.get(i).getFilename());
                    calculate = true;
                }
            } else {
                astFileList.get(i).setFAMIXrtedTree(ASTBuilder.serializeAST(astFileList.get(i)));
                ds.save(astFileList.get(i));
                System.out.println(noOfFiles-- +"\t"+astFileList.get(i).getFilename());
                calculate = true;
            }
        }
        if (calculate){
            long time2 = new Date().getTime();
            System.out.println("\nSerializing time: "+(time2 - Main.time1)/1000.0);
            DistanceCalculator.computeAPTED();
        } else {
            System.out.println("\nNothing to do...");
        }
    }

}
