package deringo.wisia.taxon;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import deringo.wisia.taxon.TaxonInformation.DetaillierteSchutzdaten;
import deringo.wisia.taxon.TaxonInformation.Schutz;
import deringo.wisia.util.Utils;

public class TaxonExtraktor {
    
    private TaxonInformation taxonInformation;
    
    public TaxonExtraktor(int knotenId) {
        taxonInformation = new TaxonInformation(knotenId);
    }

    public TaxonExtraktor extractTaxonInformation() {
        extractTaxonInformationInternal();
        return this;
    }

    public TaxonInformation getTaxonInformation() {
        return taxonInformation;
    }

    private TaxonInformation extractTaxonInformationInternal() {
        HtmlPage page = TaxonPageService.getPage(taxonInformation.knotenId);
        if (page == null) {
            System.err.println("Keine Page :(");
            return taxonInformation;
        }
        
        
        List<HtmlTableRow> o = page.getByXPath("//tr[@class='inforow']");
        
        for (HtmlTableRow row : o) {
            List<HtmlTableDataCell> taxinforubriks = row.getElementsByAttribute("td", "class", "taxinforubrik");
            List<HtmlTableDataCell> taxinfodatas = row.getElementsByAttribute("td", "class", "taxinfodata");
            
            if (taxinforubriks.size() != 1) {
                if (taxinforubriks.size() != 0) {
                    taxinforubriks.forEach(cell -> System.err.println("taxinforubrik: " + cell.asNormalizedText()));
                }
                continue;
            }
            
            extract(taxinforubriks.get(0), taxinfodatas);
        }
        
        return taxonInformation;
    }
    
    private void extract(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        String rubrik = taxinforubrik.asNormalizedText();
        switch (rubrik) {
        case "gültiger Name:":
            extractGueltigerName(taxinforubrik, taxinfodatas);
            break;
        case "Gruppe:":
            extractGruppe(taxinforubrik, taxinfodatas);
            break;
        case "Taxonomie:":
            extractTaxonomie(taxinforubrik, taxinfodatas);
            break;
        case "Synonyme und Schreibweisen:":
            extractSynonyme(taxinforubrik, taxinfodatas);
            break;
        case "Landespr. Namen:":
            extractLandesprNamen(taxinforubrik, taxinfodatas);
            break;
        case "Schutz:":
            extractSchutz(taxinforubrik, taxinfodatas);
            break;
        case "Detaillierte Schutzdaten:":
            extractDetaillierteSchutzdaten(taxinforubrik, taxinfodatas);
            break;

        default:
            if (Utils.isBlank(rubrik) && taxinfodatas.size()==0) {
//                System.out.println("Alles in Ordnung.");
            } else if (Utils.isBlank(rubrik) && taxinfodatas.size()==1) {
                extractErgaenzendeAnmerkung(taxinforubrik, taxinfodatas);
            } else {
                System.err.println("Unbekannte Rubrik in KnotenID: "+taxonInformation.knotenId);
                System.err.println(rubrik);
                System.err.println(taxinfodatas.size());
                for (HtmlTableDataCell cell : taxinfodatas) {
                    System.err.println(cell.asNormalizedText());
                }
            }
            break;
        }
    }
    
    private void extractGueltigerName(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        switch (taxinfodatas.size()) {
        case 0: {
            break;
        }
        case 1: {
            taxonInformation.gueltigerName = new TaxonInformation.GueltigerName(StringUtils.trim(taxinfodatas.get(0).asNormalizedText()), null);
            break;
        }
        case 2: {
            taxonInformation.gueltigerName = new TaxonInformation.GueltigerName(StringUtils.trim(taxinfodatas.get(0).asNormalizedText()), StringUtils.trim(taxinfodatas.get(1).asNormalizedText()));
            break;
        }
        default:
            throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
        }
    }
    
    private void extractGruppe(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        if (taxinfodatas.size() == 0) return;
        if (taxinfodatas.size() > 1) {
            throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
        }
        
        taxonInformation.gruppe = taxinfodatas.get(0).asNormalizedText().trim();
    }
    
    private void extractTaxonomie(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        if (taxinfodatas.size() == 0) return;
        if (taxinfodatas.size() > 1) {
            throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
        }
        List<String> taxonomie = new ArrayList<>();
        for (DomNode node : taxinfodatas.get(0).getChildNodes()) {
            if (node instanceof DomText) {
                taxonomie.add(node.asNormalizedText().trim());
            }
        }

        taxonInformation.taxonomie = taxonomie;
    }
    
    private void extractSynonyme(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        List<String> synonyme = new ArrayList<>();
        for (HtmlTableDataCell cell : taxinfodatas) {
            String synonym = cell.asNormalizedText().trim();
            if (!Utils.isBlank(synonym)) {
                synonyme.add(synonym);
            }
        }
        
        taxonInformation.synonyme = synonyme;
    }

    private void extractLandesprNamen(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        List<String> landesprNamen = new ArrayList<>();
        for (HtmlTableDataCell cell : taxinfodatas) {
            String landesprName = cell.asNormalizedText().trim();
            if (!Utils.isBlank(landesprName)) {
                landesprNamen.add(landesprName);
            }
        }
        
        taxonInformation.landesprNamen = landesprNamen;
    }

    private void extractSchutz(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        List<Schutz> schutzListe = new ArrayList<>();
        
        DomNodeList<HtmlElement> rows = taxinforubrik.getNextElementSibling().getElementsByTagName("tr");
        boolean firstRow = true;
        String regelwerk1 = null;
        String regelwerk2 = null;
        for (HtmlElement row : rows) {
            if (firstRow) {
                firstRow = false;
                continue;
            }
            if (row.getChildElementCount() != 6) {
                throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
            }
            if (row.getChildNodes().size() != 8) {
                throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
            }
            
            String lastLineRegelwerk1 = regelwerk1;
            String lastLineRegelwerk2 = regelwerk2;
            
            HtmlTableDataCell cell = (HtmlTableDataCell)row.getChildNodes().get(1);
            if (cell.getChildElementCount() == 1) {
                DomNodeList<DomNode> l = cell.getChildNodes();
                regelwerk1 = l.get(0).asNormalizedText().trim();
                regelwerk2 = l.get(2).asNormalizedText().trim();
            }
            
            String fussnote1 = row.getChildNodes().get(4).asNormalizedText().trim();
            String fussnote2 = row.getChildNodes().get(5).asNormalizedText().trim();
            
            String nameImRegelwerk = row.getChildNodes().get(7).asNormalizedText().trim();

            if (StringUtils.isBlank(regelwerk1) && StringUtils.isBlank(regelwerk2) && !StringUtils.isBlank(nameImRegelwerk)) {
                regelwerk1 = lastLineRegelwerk1;
                regelwerk2 = lastLineRegelwerk2;
            }
            
            Schutz schutz = new Schutz(regelwerk1, regelwerk2, fussnote1, fussnote2, nameImRegelwerk);
            schutzListe.add(schutz);
        }
        
        taxonInformation.schutzListe = schutzListe;
    }

    private void extractDetaillierteSchutzdaten(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        if (taxinfodatas.size() == 0) return;
        if (taxinfodatas.size() %3 != 0) {
            throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
        };
        
        List<DetaillierteSchutzdaten> detaillierteSchutzdatenListe = new ArrayList<>();
        for (int i=0; i<taxinfodatas.size(); ) {
            String unterschutzstellung = taxinfodatas.get(i).asNormalizedText().trim();
            String datum = taxinfodatas.get(i+1).asNormalizedText().trim();
            String bemerkung = taxinfodatas.get(i+2).asNormalizedText().trim();
            i = i+3;

            DetaillierteSchutzdaten ds = new DetaillierteSchutzdaten(unterschutzstellung, datum, bemerkung);
            detaillierteSchutzdatenListe.add(ds);
        }
        
        taxonInformation.detaillierteSchutzdatenListe = detaillierteSchutzdatenListe;
    }

    private void extractErgaenzendeAnmerkung(HtmlTableDataCell taxinforubrik, List<HtmlTableDataCell> taxinfodatas) {
        if (!Utils.isBlank(taxinforubrik.asNormalizedText())) {
            throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinforubrik.asNormalizedText());
        }
        if (taxinfodatas.size() == 0) return;
        if (taxinfodatas.size() != 1) {
            throw new IllegalArgumentException("KnotenID: "+taxonInformation.knotenId+"; Unexpected value: " + taxinfodatas.size());
        }
        taxonInformation.ergaenzendeAnmerkung = taxinfodatas.get(0).asNormalizedText().trim();
    }
}
