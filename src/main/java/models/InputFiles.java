package models;

import hac.experiment.Experiment;
import helpers.MongoDB;
import org.mongodb.morphia.Datastore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class InputFiles implements Experiment{

    private List<AST> listOfFiles;
    private long startTime;
    private long finishTime;

    public InputFiles(String directory) {
        startTime = new Date().getTime();
        System.out.println("\nSaving started...");
        this.listOfFiles = new ArrayList<>();
        try {
            listFiles(directory);
            Collections.sort(listOfFiles, new Comparator<AST>() {
                @Override
                public int compare(AST o1, AST o2) {
                    return o1.getDbShortFile().compareToIgnoreCase(o2.getDbShortFile());
                }
            });
            saveAsts();
        } catch (IOException e) {
            System.out.println("File reading error !");
            System.exit(0);
        }
    }

    private void listFiles(String directory) throws IOException {
        File dir = new File(directory);
        File[] fList = dir.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                if (file.toString().endsWith(".java")&&!file.toString().contains("package-info")){
                    String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                    this.listOfFiles.add(new AST(file.getAbsolutePath(), content.hashCode()));
                }
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath());
            }
        }
    }

    public List<AST> getListOfFiles() {
        try {
            Collections.sort(listOfFiles, new Comparator<AST>() {
                @Override
                public int compare(AST o1, AST o2) {
                    return o1.getDbShortFile().compareToIgnoreCase(o2.getDbShortFile());
                }
            });
        } catch (Exception ex){
            System.out.println("No files to analyze !");
            System.exit(0);
        }
        return listOfFiles;
    }

    private void saveAsts(){
        try{
            Datastore ds = MongoDB.INSTANCE.getDatastore();
            int noOfFiles = listOfFiles.size();
            for (int i = 0; i < listOfFiles.size(); i++) {
                if (ds.find(AST.class, "filename", listOfFiles.get(i).getFilename()).get() != null){
                    Integer version = ds.find(AST.class, "filename", listOfFiles.get(i).getFilename()).order("-version").get().getVersion();
                    Integer dbHash = ds.find(AST.class, "filename", listOfFiles.get(i).getFilename()).get().getHash();
                    Integer fileHash = listOfFiles.get(i).getHash();
                    if ( !dbHash.equals(fileHash) ){
                        listOfFiles.get(i).setVersion(++version);
                        ds.save(listOfFiles.get(i));
                    } else {
                        System.out.println("No new files to save !");
                        System.exit(0);
                    }

                } else {
                    ds.save(listOfFiles.get(i));
                    System.out.println(noOfFiles-- +"\t"+listOfFiles.get(i).getDbShortFile());
                }
            }
            finishTime = new Date().getTime();
            System.out.println("\nAll files saved to DB - duration: "+(finishTime - startTime)/1000.0+" s");
        } catch (Exception ex){
            System.out.println("Error while saving to DB !");
            System.exit(0);
        }
    }

    @Override
    public int getNumberOfObservations() {
        return this.listOfFiles.size();
    }
}
