package helpers;

import com.mongodb.MongoClient;
import models.AST;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public enum MongoDB {
    INSTANCE;
    final private Morphia morphia = new Morphia();
    private MongoClient mongoClient;

    private MongoDB() {
        try {
            if (mongoClient == null) {
                mongoClient = getClient();
                morphia.mapPackage("app.models");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MongoClient getClient() {
        try{
            return new MongoClient("localhost", 27017);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public Datastore getDatastore(){
        Datastore ds;
        ds = morphia.createDatastore(mongoClient, "metatreediff");
        morphia.map(AST.class);
        ds.ensureIndexes();
        return ds;
    }
}
