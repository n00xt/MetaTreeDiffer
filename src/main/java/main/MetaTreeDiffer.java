package main;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hac.agglomeration.*;
import helpers.MongoDB;
import models.AST;
import models.InputFiles;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import tools.TreeClusterer;
import tools.TreeSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetaTreeDiffer {

    private long startTime;
    private long finishTime;
    private String outputfile;
    private String inputDirectory;
    private boolean out, in, h;
    private AgglomerationMethod hMethod;
    private Integer treeHeight;
    private String wrongArgs = "\nWrong arguments. Try \"java -jar MetaTreeDiffer.jar -h\" for help.";
    private String helpMessage =
            "\n" +
                    "Metadata maintance based on tree distance.\n" +
                    "\n" +
                    "SYNTAX\n" +
                    "\n" +
                    "    java -jar MetaTreeDiffer.jar [-i inputDir] [-o outputFile] [-h cutoffHeight] [-c W,A,O,C,M,S,E] \n" +
                    "\n" +
                    "DESCRIPTION\n" +
                    "\n" +
                    "    Cluster source codes for metadata consistention and validation.\n" +
                    "\n" +
                    "OPTIONS\n" +
                    "\n" +
                    "    -help \n" +
                    "        print this help message.\n" +
                    "\n" +
                    "    -i INPUT_DIRECTORY,\n" +
                    "        Directory containing source code files.\n" +
                    "\n" +
                    "    -o OUTPUT_FILE, \n" +
                    "        file where to write output in JSON format.\n" +
                    "\n" +
                    "    -h CUTOFF_HEIGHT\n" +
                    "        set value for cutoff height of dendrogram\n" +
                    "\n" +
                    "    -c W,A,O,C,M,S,E \n" +
                    "        set cluster linkage method.\n" +
                    "        W - Ward\n" +
                    "        A - Average\n" +
                    "        O - Complete\n" +
                    "        C - Centroid\n" +
                    "        M - Median\n" +
                    "        S - Single\n" +
                    "        E - Weighted\n" +
                    "\n" +
                    "AUTHOR\n" +
                    "\n" +
                    "    Bc. Karol Balko";

    public static void main(String[] args) {
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        Logger morphiaLogger = Logger.getLogger("org.mongodb.morphia");
        mongoLogger.setLevel(Level.OFF);
        morphiaLogger.setLevel(Level.OFF);

        MetaTreeDiffer metaTreeDiffer = new MetaTreeDiffer();
        metaTreeDiffer.runTreeDiffer(args);
    }

    private void runTreeDiffer(String[] args){
        startTime = new Date().getTime();

        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-help")){
                    System.out.println(helpMessage);
                    System.exit(0);
                } else if (args[i].equals("-o")){
                    outputfile = args[i+1]+".json";
                    i++;
                    out = true;
                } else if (args[i].equals("-i")){
                    inputDirectory = args[i+1];
                    i++;
                    in = true;
                } else if (args[i].equals("-c")){
                    switch (args[i+1]){
                        case "W":
                            hMethod = new WardLinkage();
                            break;
                        case "A":
                            hMethod = new AverageLinkage();
                            break;
                        case "O":
                            hMethod = new CompleteLinkage();
                            break;
                        case "C":
                            hMethod = new CentroidLinkage();
                            break;
                        case "M":
                            hMethod = new MedianLinkage();
                            break;
                        case "S":
                            hMethod = new SingleLinkage();
                            break;
                        case "E":
                            hMethod = new WeightedAverageLinkage();
                            break;
                        default:
                            System.out.println("Wrong linkage method !");
                            System.exit(0);
                    }
                    i++;
                } else if (args[i].equals("-h")){
                    treeHeight = Integer.valueOf(args[i+1]);
                    i++;
                    h=true;
                } else {
                    System.out.println(wrongArgs);
                    System.exit(0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsEx){
            System.out.println("Too many arguments !");
            System.exit(0);
        } catch (NumberFormatException numberFormatEx){
            System.out.println("Wrong -h parameter !");
            System.exit(0);
        }

        if (in && out && h){
            InputFiles inputFiles = new InputFiles(inputDirectory);
            TreeSerializer treeSerializer = new TreeSerializer(inputFiles.getListOfFiles());
            TreeClusterer treeClusterer = new TreeClusterer(treeSerializer.getSerializedTrees());
            treeClusterer.clusterTrees(treeHeight, hMethod);
            try {
                exportDB();
            } catch (IOException e) {
                System.out.println("Cannot export file !");
                System.exit(0);
            }
        } else {
            System.out.println("Some of arguments missing!");
            System.exit(0);
        }
        finishTime = new Date().getTime();
        System.out.println("\nTotal duration: "+(finishTime - startTime)/1000.0+" s");

    }

    private void exportDB() throws IOException {
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        List<AST> ast = ds.find(AST.class).asList();
        File outFile = new File(outputfile);
        if(!outFile.exists()){
            outFile.createNewFile();
        }
        Path path = Paths.get(outputfile);
        try(BufferedWriter writer = Files.newBufferedWriter(path)){
            writer.write(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).setPrettyPrinting().create().toJson(ast));
        }
    }

    private static class ObjectIdTypeAdapter extends TypeAdapter<ObjectId> {
        @Override
        public void write(final JsonWriter out, final ObjectId value) throws IOException {
            out.beginObject()
                    .name("$oid")
                    .value(value.toString())
                    .endObject();
        }

        @Override
        public ObjectId read(final JsonReader in) throws IOException {
            in.beginObject();
            assert "$oid".equals(in.nextName());
            String objectId = in.nextString();
            in.endObject();
            return new ObjectId(objectId);
        }
    }

}
