package com.gmail.ahmedozmaan.unote.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AhmedOzmaan on 5/22/2016.
 */
public class Documents {
    private List<String> fileName, filePath;
    public List<String> getFileName() {
        return fileName;
    }
    public List<String> getFilePath() {
        return filePath;
    }
    public Documents() {
            this.fileName = new ArrayList<String>();
            this.filePath = new ArrayList<String>();
        }
    public void addFiLeName(String fileName){
        this.fileName.add(fileName);
    }
    public void addFiLePath(String filePath){
        this.filePath.add(filePath);
    }
}
