package deringo.wisia.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import deringo.wisia.art.Art;
import deringo.wisia.art.ArtService;
import deringo.wisia.taxon.TaxonInformationService;
import deringo.wisia.taxon.TaxonPageService;

/**
 * Exportiert eine Art in eine eigene Datei
 */
public class EineArtExporter {
    private static String folderName = "files/export";

    public static void main(String[] args) {
        int furciferViridis = 44949;
    
        int knotenId = furciferViridis;
        Art art = exportArt(knotenId);
        System.out.println(art);
    }

    public static Art exportArt(int knotenId) {
        // delete Art
        ArtService.deleteArt(knotenId);
        // delete TaxonInformation
        TaxonInformationService.deleteTaxonInformation(knotenId);
        // delete downloaded website and TaxonInformation to force reload
        TaxonPageService.deletePage(knotenId);
        
        // download and create Art
        Art art = ArtService.getArt(knotenId);
        
        // save to export folder
        saveArt(art);
        
        // return art
        return art;
    }
    
    private static String getFilename(int knotenId) {
        return (folderName + File.separator + knotenId + ".obj");
    }
    
    private static void saveArt(Art art) {
        try {
            Path pathToFile = Paths.get(getFilename(art.getKnoten_id()));
            Files.createDirectories(pathToFile.getParent());

            FileOutputStream fos = new FileOutputStream(getFilename(art.getKnoten_id()));
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(art);
            
            oos.close();
            fos.close();
         } catch (IOException e) {
            System.err.println("An error occurred: " + e);
         }
    }
}