package tools;

import com.apporiented.algorithm.clustering.*;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import helpers.MongoDB;
import main.Main;
import models.AST;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;


public final class HClustering {

    private static int j = 0;
    private static String lastCluster;
    private static Datastore ds;

    public static void clusterFiles(String[] names, double[][] distances){
        long time1 = new Date().getTime();
        ds = MongoDB.INSTANCE.getDatastore();
        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.performClustering(distances, names, new CompleteLinkageStrategy());
//        Cluster cluster = alg.performClustering(distances, names, new WeightedLinkageStrategy());
        showDendogram(cluster);
        showCluster(cluster);
        long time2 = new Date().getTime();
        System.out.println("\nHClust time: "+(time2 - time1)/1000.0);
        try {
            exportDB();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void exportDB() throws IOException {
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        List<AST> ast = ds.find(AST.class).asList();
        File outFile = new File(Main.outputFile);
        if(!outFile.exists()){
            outFile.createNewFile();
        }
        Path path = Paths.get(Main.outputFile);
        try(BufferedWriter writer = Files.newBufferedWriter(path)){
            writer.write(new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).setPrettyPrinting().create().toJson(ast));
        }
    }

    public static void clusterFiles1(String[] names, double[][] distances){
        long time1 = new Date().getTime();
        ds = MongoDB.INSTANCE.getDatastore();
        ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
        Cluster cluster = alg.performClustering(distances, names, new WeightedLinkageStrategy());
        showDendogram(cluster);
        showCluster1(cluster);
        long time2 = new Date().getTime();
        System.out.println("\nHClust time: "+(time2 - time1)/1000.0);
    }

    public static void showCluster(Cluster cluster){
        for (int i = 0; i < cluster.getChildren().size(); i++) {
            if (cluster.getChildren().get(i).getDistanceValue()>0){
                if (!cluster.getName().equalsIgnoreCase(lastCluster)){
                    j++;
                    System.out.println(cluster.getName()+" "+cluster.getDistanceValue()+" j="+j);
                }
                lastCluster = cluster.getName();
            }
            if (cluster.getChildren().get(i).isLeaf()){
                System.out.println(cluster.getChildren().get(i).getName()+" j="+j);
                final Query<AST> setClusterQuery = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(new ObjectId(cluster.getChildren().get(i).getName()));
                final UpdateOperations updateAST = ds.createUpdateOperations(AST.class).set("clusterId", j);
                final UpdateResults results = ds.updateFirst(setClusterQuery, updateAST);
            }
            showCluster(cluster.getChildren().get(i));
        }
    }

    public static void showCluster1(Cluster cluster){
        if (cluster.isLeaf()){
            System.out.println(cluster.getName()+" "+cluster.getDistance().toString());
            System.out.println();
        }
        for (int i = 0; i < cluster.getChildren().size(); i++) {
            if (cluster.getDistanceValue()<4){
                System.out.println(cluster.getName()+" "+cluster.getDistance().toString());
                showCluster1(cluster.getChildren().get(i));
            } else {
                System.out.println(cluster.getName()+" "+cluster.getDistance().toString());
                System.out.println("----------------------");
                showCluster1(cluster.getChildren().get(i));
            }

        }
//        for (int i = 0; i < cluster.getChildren().size(); i++) {
//            if (cluster.getChildren().get(i).getDistanceValue()>0){
//                if (!cluster.getName().equalsIgnoreCase(lastCluster)){
//                    j++;
//                    System.out.println(cluster.getName()+" "+cluster.getDistanceValue()+" j="+j);
//                }
//                lastCluster = cluster.getName();
//            }
//            if (cluster.getChildren().get(i).isLeaf()){
//                System.out.println(cluster.getChildren().get(i).getName()+" j="+j);
//                final Query<AST> setClusterQuery = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(new ObjectId(cluster.getChildren().get(i).getName()));
//                final UpdateOperations updateAST = ds.createUpdateOperations(AST.class).set("clusterId", j);
//                final UpdateResults results = ds.updateFirst(setClusterQuery, updateAST);
//            }
//            showCluster(cluster.getChildren().get(i));
//        }
    }

    public static void showDendogram(Cluster cluster){
        JFrame frame = new JFrame();
        frame.setSize(1900, 1000);
        frame.setLocation(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel content = new JPanel();
        DendrogramPanel dp = new DendrogramPanel();
        frame.setContentPane(content);
        content.setBackground(Color.red);
        content.setLayout(new BorderLayout());
        content.add(dp, BorderLayout.CENTER);
        dp.setBackground(Color.WHITE);
        dp.setLineColor(Color.BLACK);
        dp.setScaleValueDecimals(0);
        dp.setScaleValueInterval(1);
        dp.setShowDistances(false);
        dp.setModel(cluster);
        frame.setVisible(true);
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
