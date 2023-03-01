package deringo.wisia.fussnote;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class FussnoteService {
    private static String FUSSNOTEN = "https://www.wisia.de/ShowFNDef";

    public static List<Fussnote> getFussnoten() {
        HtmlPage page = getPage();
        List<Fussnote> fussnoten = extractFussnoten(page);
        return fussnoten;
    }
    
    private static List<Fussnote> extractFussnoten(HtmlPage page) {
        List<Fussnote> fussnoten = new ArrayList<>();
        if (page == null) return fussnoten;
        
        List<HtmlAnchor> as = page.getByXPath("//a[@name]");
        for (HtmlAnchor a : as) {
            HtmlHeading2 h = (HtmlHeading2) a.getNextElementSibling();
            String regelwerk = h.asNormalizedText();
            HtmlTable ht = (HtmlTable) h.getNextElementSibling();
            for (HtmlTableRow row : ht.getBodies().get(0).getRows()) {
                if (row.getCells().size() == 2) {
                    String fussnoteId = row.getCells().get(0).asNormalizedText();
                    String fussnoteText = row.getCells().get(1).asNormalizedText();
                    Fussnote fussnote = new Fussnote(regelwerk, fussnoteId, fussnoteText);
                    fussnoten.add(fussnote);
                }
            }
        }

        return fussnoten;
    }
    
    private static HtmlPage getPage() {
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
            HtmlPage page = webClient.getPage(FUSSNOTEN);
            return page;
        } catch (Exception e) {
            System.err.println("An error occurred:\n" + e);
            return null;
        }
    }
}
