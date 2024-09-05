package deringo.wisia.art;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import deringo.wisia.taxon.TaxonInformationTransformator;

public class ArtService {
    private static String folderName = "files/art";
    private static String fileName = "art";

    public static Art getArt(Integer knotenId) {
        return getArt(knotenId, false);
    }
    
    public static Art getArt(Integer knotenId, boolean loadOnly) {
        Art art = loadArt(knotenId);
        if (art == null && !loadOnly) {
            art = TaxonInformationTransformator.transform(knotenId);
            if (art != null) {
                saveArt(knotenId);
            }
        }
        return art;
    }
    
    private static Art loadArt(Integer knotenId) {
        try {
            FileInputStream fis = new FileInputStream(new File(getFilename(knotenId)));
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            
            Art art = (Art) ois.readObject();
            
            ois.close();
            fis.close();
            
            return art;
            
        } catch (FileNotFoundException e) {
            // do nothing
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred (knotenId: "+knotenId+"): " + e);
            return null;
        } catch (Exception e) {
            System.err.println("An error occurred (knotenId: "+knotenId+"): " + e);
            return null;
        }
    }
    
    private static void saveArt(Integer knotenId) {
        Art art = TaxonInformationTransformator.transform(knotenId);

        try {
            Path pathToFile = Paths.get(getFilename(knotenId));
            Files.createDirectories(pathToFile.getParent());

            FileOutputStream fos = new FileOutputStream(getFilename(knotenId));
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(art);
            
            oos.close();
            fos.close();
         } catch (IOException e) {
            System.err.println("An error occurred: " + e);
         }
        System.out.println("save Art" + knotenId);
    }
    
    private static String getFilename(Integer knotenId) {
        return (folderName + File.separator + (knotenId/1000) + File.separator + fileName + knotenId);
    }

    public static void deleteArt(int knotenId) {
        Path pathToFile = Paths.get(getFilename(knotenId));
        try {
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}