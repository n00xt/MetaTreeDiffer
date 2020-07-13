package tools;


import hac.HierarchicalAgglomerativeClusterer;
import hac.agglomeration.AgglomerationMethod;
import hac.dendrogram.*;
import hac.experiment.DissimilarityMeasure;
import hac.experiment.Experiment;
import distance.APTED;
import helpers.MongoDB;
import models.AST;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import util.LblTree;
import java.util.Date;
import java.util.List;

public class TreeClusterer implements DissimilarityMeasure, Experiment{
    private APTED apted;
    private List<AST> astList;
    private Dendrogram dendrogram;
    private int i;
    private long startTime;
    private long finishTime;


    public TreeClusterer(List<AST> astList) {
        startTime = new Date().getTime();
        System.out.println("\nClustering started...\n");
        apted = new APTED(1,1,0);
        this.astList = astList;
        this.i = 0;
    }

    public void clusterTrees(Integer h, AgglomerationMethod method){
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(getNumberOfObservations());
        HierarchicalAgglomerativeClusterer hClusterer = new HierarchicalAgglomerativeClusterer(this, this, method);
        hClusterer.cluster(dendrogramBuilder);
        this.dendrogram = dendrogramBuilder.getDendrogram();
        getClustersByHeight(h, dendrogram.getRoot(), ds);
        finishTime = new Date().getTime();
        System.out.println("\nClustering finished - duration: "+(finishTime - startTime)/1000.0+" s\n");
    }

    private void saveDistMatrix(HierarchicalAgglomerativeClusterer clusterer, Datastore ds){
        for (int j = 0; j < astList.size(); j++) {
            final Query<AST> setMatrixQuery = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(astList.get(i).getId());
            final UpdateOperations updateAST = ds.createUpdateOperations(AST.class).set("dist", clusterer.getDmatrix()[i]);
        }
    }

    private void saveCluster(Integer observation, Datastore ds){
        if (observation != null){
            final Query<AST> setClusterQuery = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(astList.get(observation).getId());
            final UpdateOperations updateAST = ds.createUpdateOperations(AST.class).set("clusterId", i);
            final UpdateResults results = ds.updateFirst(setClusterQuery, updateAST);
        }
    }

    private void getClustersByHeight(Integer h, final DendrogramNode node, Datastore ds){
        System.out.print(".");
        if (node == null){

        } else if (node instanceof ObservationNode){
            saveCluster(((ObservationNode) node).getObservation(), ds);
        } else if (node instanceof MergeNode){
            if (((MergeNode) node).getDissimilarity() <= h){
                getClustersByHeight(h, node.getLeft(), ds);
                getClustersByHeight(h, node.getRight(), ds);
            } else {
                getClustersByHeight(h, node.getLeft(), ds);
                getClustersByHeight(h, node.getRight(), ds);
                i++;
            }
        }
    }

    @Override
    public double computeDissimilarity(Experiment experiment, int observation1, int observation2) {
        LblTree lt1 = LblTree.fromString(astList.get(observation1).getFAMIXrtedTree());
        LblTree lt2 = LblTree.fromString(astList.get(observation2).getFAMIXrtedTree());
        return apted.nonNormalizedTreeDist(lt1, lt2);
    }

    @Override
    public int getNumberOfObservations() {
        return astList.size();
    }
}
