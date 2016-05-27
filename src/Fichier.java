import java.util.*;
import java.io.File.*;
import java.io.*;

public class Fichier extends Application{
    public final int maxSize = 466;

    public Fichier(Entity e){
        this.entity = e;
        this.id = "TRANS###";
    }
    
    public static String lengthToNW(int length){
        String str = "" + length;
        while(str.length() < 3){
            str = "0" + str;
        }
        return str;
    }

    public static String getReq(String msg){
        return msg.substring(27); // TODO
    }

    public static String getNomFichier(String msg){
        return msg.substring(27); // TODO
    }

    public String getRokRequest(String numTrans, String nomFichier, int nbMsg){
        return entity.getAppRequest(this.id, "ROK " + numTrans + " " + nomFichier.length() + " " + nomFichier + " " + nbMsg);
    } 

    public String getSenRequest(String numTrans, String content){
        return entity.getAppRequest(this.id, "SEN " + Entity.generateIDM() + " " + numTrans + " " + content.length() + " " + content);
    }

    public void handleApp(String msg){
        if (getReq(msg) == "REQ"){ // Testons si l'entity possède le fichier nom-fichier
            String nomFichier = getNomFichier(msg);
            File f = new File(nomFichier);
            if(f.exists() && !f.isDirectory()) { 
                int nbMsg = ((int)f.length() / maxSize) + 1;
                String numTrans = Entity.generateIDM();
                entity.sendUDP(getRokRequest(numTrans, nomFichier, nbMsg));

                try{
                    FileInputStream fis = new FileInputStream(f);
                    byte[] data = new byte[(int) f.length()];
                    fis.read(data);
                    fis.close();
                    String allFileContent = new String(data, "UTF-8");
                    for (int i = 0; i < nbMsg; i++){
                        entity.sendUDP(getSenRequest(numTrans, allFileContent.substring(i*maxSize,maxSize + (i*maxSize) + 1)));
                    }
                } catch (Exception e){
                    System.out.println("Impossible de charger le fichier !");
                }
            }
            else{
                entity.sendUDP(msg); // Si ne possède pas le fichier, on envoie la requete au suivant !
            }
        }
    }

    public void useApp(){
        
    }

    public void useApp(String msg){
        
    }
}