package main;

import ch.usi.inf.sape.hac.agglomeration.CompleteLinkage;
import ch.usi.inf.sape.hac.agglomeration.MedianLinkage;
import ch.usi.inf.sape.hac.agglomeration.SingleLinkage;
import ch.usi.inf.sape.hac.agglomeration.WardLinkage;
import helpers.MongoDB;
import models.AST;
import org.mongodb.morphia.Datastore;
import tools.ASTBuilder;
import tools.DistanceCalculator;
import tools.DistanceComputer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static long time1;
    public static String outputFile;
    public String outputfile;

    public static void main(String[] args) {
        new DistanceComputer(new WardLinkage());
//        if (args.length!=2){
//            System.out.println("Bad inputs!");
//            System.exit(1);
//        }
//        outputFile = args[1];
//        time1 = new Date().getTime();
//        List<AST> astFileList;
//        try {
//            astFileList = new ArrayList<>();
//            listFiles(args[0], astFileList);
//            saveFilesToDB(astFileList);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void listFiles(String directoryName, List<AST> astFileList) throws IOException {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                if (file.toString().endsWith(".java")&&!file.toString().contains("package-info")){
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
                    astFileList.get(i).setShortFilename(astFileList.get(i).getDbShortFile());
                    ds.save(astFileList.get(i));
//                    System.out.println(noOfFiles-- +"\t"+astFileList.get(i).getFilename());
                    calculate = true;
                }
            } else {
                astFileList.get(i).setFAMIXrtedTree(ASTBuilder.serializeAST(astFileList.get(i)));
                astFileList.get(i).setShortFilename(astFileList.get(i).getDbShortFile());
                ds.save(astFileList.get(i));
                System.out.println(noOfFiles-- +"\t"+astFileList.get(i).getDbShortFile());
                calculate = true;
            }
        }
        if (calculate){
            long time2 = new Date().getTime();
            System.out.println("\nSerializing time: "+(time2 - Main.time1)/1000.0);
            new DistanceComputer(new WardLinkage());
//            DistanceCalculator.computeAPTED();
        } else {
            System.out.println("\nNothing to do...");
        }
    }

}
