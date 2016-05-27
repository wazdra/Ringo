import java.util.*;
import java.io.File.*;
import java.io.*;

public class Fichier extends Application{
    public final int maxSize = 466;
    public String nomFichierAttendu = "";
    public int nbMessages = 0;
    public String idTrans = "";
    public int numMessage = 0;

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

    public static String getNomFichierREQ(String msg){
        return msg.substring(27); // TODO
    }

    public static String getNomFichierROK(String msg){
        return msg.substring(27); // TODO
    }

    public static int getNbFichiersAttendus(String msg){
        return Integer.parseInt(msg.substring(27)); // TODO
    }

    public static int getNoMessSEN(String msg){
        return Integer.parseInt(msg.substring(27)); // TODO
    }

    public static String getIdTransROK(String msg){
        return msg.substring(27); // TODO
    }

    public static String getIdTransSEN(String msg){
        return msg.substring(27); // TODO
    }

    public String getRokRequest(String numTrans, String nomFichier, int nbMsg){
        return entity.getAppRequest(this.id, "ROK " + numTrans + " " + nomFichier.length() + " " + nomFichier + " " + nbMsg);
    } 

    public String getSenRequest(String numTrans, String content){
        return entity.getAppRequest(this.id, "SEN " + Entity.generateIDM() + " " + numTrans + " " + content.length() + " " + content);
    }

    public void handleApp(String msg){
        switch (getReq(msg)){
            case "REQ":
                // Testons si l'entity possède le fichier nom-fichier
                String nomFichier = getNomFichierREQ(msg);
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
                        System.out.println("Impossible de charger les données du fichier !");
                    }
                }
                else{
                    entity.sendUDP(msg); // Si ne possède pas le fichier, on envoie la requete au suivant !
                }
                break;
            case "ROK":
                if (this.nomFichierAttendu == getNomFichierROK(msg)){
                    // On réccupère les données qui vont être envoyées
                    this.nbMessages = getNbFichiersAttendus(msg);
                    this.idTrans = getIdTransROK(msg);
                    // Création d'un nouveau fichier
                    try{      
                        File file = new File("transfert.txt");
                        if (file.createNewFile()){
                            System.out.println("File is created!");
                        }else{
                            System.out.println("File already exists.");
                        }
                    } catch(Exception e){
                        System.out.println("Impossible de créer un fichier");
                    }
                }
                else{
                    entity.sendUDP(msg);
                }
                break;
            case "SEN":
                if (this.idTrans == getIdTransSEN(msg)){ // Si l'id de transition de message correspond
                    if (getNoMessSEN(msg) == this.numMessage){

                        this.numMessage++;
                    }
                    else{
                        System.out.println("Paquets reçus dans le mauvais ordre");
                    }
                }
                break;
            default:
                System.out.println("Erreur de requête");
                break;
        }
    }

    public void useApp(){
        
    }

    public void useApp(String msg){
        
    }
}