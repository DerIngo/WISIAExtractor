package deringo.wisia.taxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TaxonInformationService {
    private static String folderName = "files/taxon";
    private static String fileName = "knoten";

    
    public static TaxonInformation getTaxonInformation(int knotenId) {
        TaxonInformation page = loadTaxonInformation(knotenId);
        if (page == null) {
            saveTaxonInformation(knotenId);
            page = loadTaxonInformation(knotenId);
        }
        if (page == null) {
            System.err.println("Keine TaxonInformation: " + knotenId);
        }
        return page;
    }
    
    private static TaxonInformation loadTaxonInformation(int knotenId) {
        try {
            FileInputStream fis = new FileInputStream(new File(getFilename(knotenId)));
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            
            TaxonInformation taxonInformation = (TaxonInformation) ois.readObject();
            
            ois.close();
            fis.close();
            
            return taxonInformation;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred: " + e);
            return null;
        }
    }
    
    private static void saveTaxonInformation(int knotenId) {
        TaxonInformation taxonInformation = new TaxonExtraktor(knotenId)
                .extractTaxonInformation()
                .getTaxonInformation();

        try {
            Path pathToFile = Paths.get(getFilename(knotenId));
            Files.createDirectories(pathToFile.getParent());

            FileOutputStream fos = new FileOutputStream(getFilename(knotenId));
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(taxonInformation);
            
            oos.close();
            fos.close();
         } catch (IOException e) {
            System.err.println("An error occurred: " + e);
         }
        System.out.println("saveTaxonInformation" + knotenId);
    }
    
    private static String getFilename(int knotenId) {
        return (folderName + File.separator + (knotenId/1000) + File.separator + fileName + knotenId);
    }
    
    public static boolean isEmpty(TaxonInformation information) {
        if (information.gueltigerName == null) {
            return true;
        }
        
        if (information.taxonomie == null) {
            System.out.println("(knotenId: " + information.knotenId + ") " +"taxonomie NULL");
            return true;
        }

        return false;
    }
}
