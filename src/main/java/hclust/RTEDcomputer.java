package hclust;

import analyzers.APTED.distance.APTED;
import analyzers.APTED.util.LblTree;
import ch.usi.inf.sape.hac.experiment.DissimilarityMeasure;
import ch.usi.inf.sape.hac.experiment.Experiment;

public class RTEDcomputer implements DissimilarityMeasure {

    public RTEDcomputer(){

    }

    @Override
    public double computeDissimilarity(Experiment experiment, int observation1, int observation2) {
        LblTree lblTree1 = LblTree.fromString(experiment.getObservations().get(observation1).getFAMIXrtedTree());
        LblTree lblTree2 = LblTree.fromString(experiment.getObservations().get(observation2).getFAMIXrtedTree());
        return new APTED(1,1,0).nonNormalizedTreeDist(lblTree1, lblTree2);
    }
}
