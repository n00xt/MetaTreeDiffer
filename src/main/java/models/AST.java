package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

@Entity(value = "ast", noClassnameStored = true)
public class AST {
    @Id
    private ObjectId id;
    private String filename;
    private String shortFilename;
    private String dbShortFile;
    private String RTEDTree;
    private String ASTree;
    private String TokensTree;
    private String FAMIXTree;
    private String FAMIXrtedTree;
    private LinkedHashMap<ObjectId, Double> distance;
    private double[] dist;
    private Integer clusterId;
    private Integer version;
    private int hash;
    private List<AST> listOfFiles;
    private String directory;

    public AST() {
    }

    public AST(String filename, int hash) {
        this.filename = filename;
        this.hash = hash;
        this.version = 0;
        setDbShortFile();
        setShortFilename();
    }

    public double[] getDist() {
        return dist;
    }

    public void setDist(double[] dist) {
        this.dist = dist;
    }

    public String getShortFilename() {
        if (shortFilename.isEmpty() || shortFilename == null){
            setShortFilename();
            return shortFilename;
        } else {
            return shortFilename;
        }
    }

    public String getDbShortFile() {
        if (dbShortFile.isEmpty() || dbShortFile == null){
            setDbShortFile();
            return dbShortFile;
        } else {
            return dbShortFile;
        }
    }

    public void setDbShortFile() {
        this.dbShortFile = this.filename.substring(this.filename.lastIndexOf("/") + 1).replace(".java","");
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public void setShortFilename() {
        this.shortFilename = this.version+this.filename.substring(this.filename.lastIndexOf("/") + 1).replace(".java","");
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getRTEDTree() {
        return RTEDTree;
    }

    public void setRTEDTree(String RTEDTree) {
        this.RTEDTree = RTEDTree;
    }

    public String getASTree() {
        return ASTree;
    }

    public void setASTree(String ASTree) {
        this.ASTree = ASTree;
    }

    public String getTokensTree() {
        return TokensTree;
    }

    public void setTokensTree(String tokensTree) {
        this.TokensTree = tokensTree;
    }

    public String getFAMIXTree() {
        return FAMIXTree;
    }

    public void setFAMIXTree(String FAMIXTree) {
        this.FAMIXTree = FAMIXTree;
    }

    public String getFAMIXrtedTree() {
        return FAMIXrtedTree;
    }

    public void setFAMIXrtedTree(String FAMIXrtedTree) {
        this.FAMIXrtedTree = FAMIXrtedTree;
    }

    public LinkedHashMap<ObjectId, Double> getDistance() {
        return distance;
    }

    public void setDistance(LinkedHashMap<ObjectId, Double> distance) {
        this.distance = distance;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
