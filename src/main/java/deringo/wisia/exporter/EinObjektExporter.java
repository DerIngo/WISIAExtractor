package deringo.wisia.exporter;

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
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import deringo.wisia.WisiaExtraktor;
import deringo.wisia.art.Art;

/**
 * Exportiert die Liste aller Wisia Arten in eine einzige Datei.<br/>
 * Importiert die zuvor exportierte Datei.
 *
 */
public class EinObjektExporter {
    private static String folderName = "files/export";
    private static String fileName = "alleArten.obj";

    public static void main(String[] args) {
      long start = System.currentTimeMillis();
      List<Art> alleArten = WisiaExtraktor.getAllArt();
      long end = System.currentTimeMillis();
      long durationInSec = (end - start) / 1000;
      String message = String.format("Alle %d Arten geladen in %d Sekunden.", alleArten.size(), durationInSec);
      System.out.println(message);
      
      start = System.currentTimeMillis();
      EinObjektExporter.export(alleArten);
      end = System.currentTimeMillis();
      durationInSec = (end - start) / 1000;
      message = String.format("Alle %d Arten exportiert in %d Sekunden.", alleArten.size(), durationInSec);
      System.out.println(message);
      
      start = System.currentTimeMillis();
      alleArten = EinObjektExporter.importArten();
      end = System.currentTimeMillis();
      durationInSec = (end - start) / 1000;
      message = String.format("Alle %d Arten importiert in %d Sekunden.", alleArten.size(), durationInSec);
      System.out.println(message);
    }
    
    public static void export(List<Art> arten) {
        saveArt(arten);
    }
    
    public static List<Art> importArten() {
        return loadArten();
    }
    
    private static void saveArt(List<Art> arten) {
        try {
            Path pathToFile = Paths.get(getFilename());
            Files.createDirectories(pathToFile.getParent());

            FileOutputStream fos = new FileOutputStream(getFilename());
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(arten);
            
            oos.close();
            fos.close();
         } catch (IOException e) {
            System.err.println("An error occurred: " + e);
         }
        System.out.println("save Arten size: " + arten.size());
    }
    
    private static List<Art> loadArten() {
        try {
            FileInputStream fis = new FileInputStream(new File(getFilename()));
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            
            @SuppressWarnings("unchecked")
            List<Art> art = (List<Art>) ois.readObject();
            
            ois.close();
            fis.close();
            
            return art;
            
        } catch (FileNotFoundException e) {
            System.err.println("Datei nicht gefunden: " + getFilename());
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred: " + e);
            return null;
        } catch (Exception e) {
            System.err.println("An error occurred: " + e);
            return null;
        }
    }
    
    private static String getFilename() {
        return (folderName + File.separator + fileName);
    }
}
