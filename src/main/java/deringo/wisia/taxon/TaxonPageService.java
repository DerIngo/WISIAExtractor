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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TaxonPageService {
    private static String TAXONINFORMATION = "https://www.wisia.de/GetTaxInfo?knoten_id=";
    
    private static String folderName = "files/pages";
    private static String fileName = "knoten";

    public static HtmlPage getPage(int knotenId) {
        HtmlPage page = loadPage(knotenId);
        if (page == null) {
            savePage(knotenId);
            page = loadPage(knotenId);
        }
        if (page != null && !checkORA01000(page, knotenId)) {
            TaxonPageService.deletePage(knotenId);
            System.err.println("ORA-01000: maximum open cursors exceeded found. Try to reload once.");
            page = loadPage(knotenId);
            if (page != null && !checkORA01000(page, knotenId)) {
                TaxonPageService.deletePage(knotenId);
                System.err.println("ORA-01000: maximum open cursors exceeded found. Please try to reload later.");
                page = null;
            }
        }
        
        if (page == null) {
            System.err.println("Keine Page :(");
        }
        return page;
    }
    
    private static final String ORA01000 = "ORA-01000: maximum open cursors exceeded";
    private static boolean checkORA01000(HtmlPage page, int knotenId) {
        if (!page.asNormalizedText().contains(ORA01000)) {
            return true;
        }
        return false;
    }
    
    private static HtmlPage loadPage(int knotenId) {
        try {
            FileInputStream fis = new FileInputStream(new File(getFilename(knotenId)));
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            
            HtmlPage page = (HtmlPage) ois.readObject();
            
            ois.close();
            fis.close();
            
            return page;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("An error occurred (knotenId: "+knotenId+"): " + e);
            return null;
        } catch (Exception e) {
            System.err.println("An error occurred (knotenId: "+knotenId+"): " + e);
            return null;
        }
    }
    
    private static void savePage(int knotenId) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.addRequestHeader("Accept-Charset", "utf-8");
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getCurrentWindow().getJobManager().removeAllJobs();
        webClient.close();
        
        try {
            HtmlPage page = webClient.getPage(TAXONINFORMATION + knotenId);
            
            Path pathToFile = Paths.get(getFilename(knotenId));
            Files.createDirectories(pathToFile.getParent());

            FileOutputStream fos = new FileOutputStream(getFilename(knotenId));
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(page);
            
            oos.close();
            fos.close();
         } catch (IOException e) {
            System.err.println("An error occurred: " + e);
         }
    }
    
    private static String getFilename(int knotenId) {
        return (folderName + File.separator + (knotenId/1000) + File.separator + fileName + knotenId);
    }

    public static void deletePage(int knotenId) {
        Path pathToFile = Paths.get(getFilename(knotenId));
        try {
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
