package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.ArrayList;

@Entity(value = "distmatrix", noClassnameStored = true)
public class DMatrix {
    @Id
    private ObjectId id;
    private double[][] matrix;
    private ArrayList<String> shortFilename;

    public DMatrix() {
        this.shortFilename = new ArrayList<String>();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public double[][] getMatrix() {
        return matrix;
    }

//    public void setMatrix(double ob1, double ob2, int amount) {
//        this.matrix = new double[amount][amount];
//        for (int i = 0; i < amount; i++) {
//            for (int j = 0; j < amount; j++) {
//                matrix[i][j] =
//            }
//        }
//        this.matrix = matrix;
//    }

    public ArrayList<String> getShortFilename() {
        return shortFilename;
    }

    public void setShortFilename(String shortFilename) {
        this.shortFilename.add(shortFilename);
    }
}
