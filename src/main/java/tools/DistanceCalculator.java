package tools;

import analyzers.APTED.distance.APTED;
import analyzers.APTED.util.LblTree;
import helpers.MongoDB;
import models.AST;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public final class DistanceCalculator {

    public static void computeAPTED(){
        long time1 = new Date().getTime();
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        List<AST> astList = ds.find(AST.class).field("FAMIXrtedTree").exists().asList();
        LinkedHashMap<ObjectId, Double> distances;
        String[] filenames = new String[astList.size()];
//        String[] filenames1 = new String[astList.size()];
        double[][] distanceMatrix = new double[astList.size()][astList.size()];
//        double[][] distanceMatrix1 = new double[astList.size()][astList.size()];

        for (int i = 0; i < astList.size(); i++) {
            distances = new LinkedHashMap<>();
            filenames[i] = astList.get(i).getId().toHexString();
//            filenames1[i] = astList.get(i).getShortFilename();
            for (int j = 0; j < astList.size(); j++) {
                LblTree lblTree1 = LblTree.fromString(astList.get(i).getFAMIXrtedTree());
                LblTree lblTree2 = LblTree.fromString(astList.get(j).getFAMIXrtedTree());
                double distance = new APTED(1,1,0).nonNormalizedTreeDist(lblTree1, lblTree2);
                distances.put(astList.get(j).getId(), distance);
                distanceMatrix[i][j] = distance;
//                distanceMatrix1[i][j] = distance;
                System.out.println(i+","+j+"\t"+distance);
            }
            final Query<AST> updateASTreeQuery = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(astList.get(i).getId());
            final UpdateOperations setDistanceOperations = ds.createUpdateOperations(AST.class).set("distance", distances);
            final UpdateResults updateASTree = ds.updateFirst(updateASTreeQuery, setDistanceOperations);
        }
        long time2 = new Date().getTime();
        System.out.println("\nRTED time: "+(time2 - time1)/1000.0);

        HClustering.clusterFiles(filenames, distanceMatrix);
//        HClustering.clusterFiles1(filenames1, distanceMatrix1);
    }

}
