package com.gmail.ahmedozmaan.unote.model;

/**
 * Created by AhmedOzmaan on 5/22/2016.
 */
public class DeviceDocuments {
   public Documents PDF, DOC, PPT ,TXT;
    String selectedFilePath;
    int flag;
    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getSelectedFilePath(int position) {
        if(flag == 1){
            selectedFilePath = PDF.getFilePath().get(position);
        }else  if(flag == 2){
            selectedFilePath = PPT.getFilePath().get(position);
        }else  if(flag == 3){
            selectedFilePath = DOC.getFilePath().get(position);
        }else  if(flag == 4){
            selectedFilePath = TXT.getFilePath().get(position);
        }
        return selectedFilePath;
    }
    public  DeviceDocuments(){
        PDF = new Documents();
        DOC = new Documents();
        PPT = new Documents();
        TXT = new Documents();
    }
}

