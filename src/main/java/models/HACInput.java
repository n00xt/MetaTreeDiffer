package models;

import ch.usi.inf.sape.hac.experiment.Experiment;
import helpers.MongoDB;
import org.mongodb.morphia.Datastore;

import java.util.List;

public class HACInput implements Experiment {

    private List<AST> asts;

    public HACInput() {
        Datastore ds = MongoDB.INSTANCE.getDatastore();
        asts = ds.find(AST.class).field("FAMIXrtedTree").exists().asList();
    }

    @Override
    public int getNumberOfObservations() {
        return asts.size();
    }

    @Override
    public List<AST> getObservations() {
        return asts;
    }

}
