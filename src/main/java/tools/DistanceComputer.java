package tools;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.AgglomerationMethod;
import ch.usi.inf.sape.hac.dendrogram.*;
import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;
import hclust.RTEDcomputer;
import helpers.MongoDB;
import models.AST;
import models.HACInput;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.ArrayList;
import java.util.List;

public class DistanceComputer {

    Dendrogram dendrogram;
    List<AST> astList;
    Datastore ds;
    int i;

    public DistanceComputer(AgglomerationMethod agglomerationMethod){
        ds = MongoDB.INSTANCE.getDatastore();
        Experiment experiment = new HACInput();
        DissimilarityMeasure dissimilarityMeasure = new RTEDcomputer();
        DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(experiment.getNumberOfObservations());
        HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);
        clusterer.cluster(dendrogramBuilder);
        dendrogram = dendrogramBuilder.getDendrogram();
        astList = experiment.getObservations();
        i = 0;
        saveDistMatrix(clusterer);
        saveClusters(dendrogram.getRoot());
    }

    public Dendrogram getDendrogram() {
        return dendrogram;
    }

    private void saveClusters (final DendrogramNode node){
            if (node==null) {

            } else if (node instanceof ObservationNode) {
                final Query<AST> setClusterQuery = ds.createQuery(AST.class).field(Mapper.ID_KEY).equal(astList.get(((ObservationNode) node).getObservation()).getId());
                final UpdateOperations updateAST = ds.createUpdateOperations(AST.class).set("clusterId", i);
                final UpdateResults results = ds.updateFirst(setClusterQuery, updateAST);
            } else if (node instanceof MergeNode) {
                if (((MergeNode) node).getDissimilarity() < 5) {
                    saveClusters(((MergeNode) node).getLeft());
                    saveClusters(((MergeNode) node).getRight());
                } else {
                    i++;
                    saveClusters(((MergeNode) node).getLeft());
                    saveClusters(((MergeNode) node).getRight());
                }
            }
    }

    private void saveDistMatrix(HierarchicalAgglomerativeClusterer clusterer){
        List<Document> dList = new ArrayList<>();
        for (int j = 0; j < clusterer.getDmatrix().length; j++) {
            Document document = new Document();
            List<Double>  l = new ArrayList<>();
            document.put("file", astList.get(j).getShortFilename());
            for (int k = 0; k < clusterer.getDmatrix()[j].length; k++) {
                l.add(clusterer.getDmatrix()[j][k]);
            }
            document.put("distances", l);
            MongoDB.INSTANCE.getClient().getDatabase("metadiff").getCollection("distmatrix").insertOne(document);
        }
    }
}
