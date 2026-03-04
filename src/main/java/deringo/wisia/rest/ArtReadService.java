package deringo.wisia.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import deringo.wisia.art.Anhang;
import deringo.wisia.art.Art;
import deringo.wisia.art.Regelwerk;
import deringo.wisia.exporter.EinObjektExporter;
import jakarta.annotation.PostConstruct;

@Service
public class ArtReadService {
    private static final Logger LOG = LoggerFactory.getLogger(ArtReadService.class);

    private final String dataFile;

    private List<Art> allArts = List.of();
    private Map<Integer, Art> byKnotenId = Map.of();

    public ArtReadService(@Value("${wisia.data.file:files/export/alleArten.obj}") String dataFile) {
        this.dataFile = dataFile;
    }

    @PostConstruct
    public synchronized void loadData() {
        List<Art> loaded = EinObjektExporter.importArten(dataFile);
        if (loaded == null) {
            LOG.warn("Could not load data from '{}', trying classpath resource 'alleArten.obj'.", dataFile);
            loaded = loadFromResource();
        }
        if (loaded == null) {
            LOG.warn("No art data available. Starting with empty dataset.");
            allArts = List.of();
            byKnotenId = Map.of();
            return;
        }

        loaded.removeIf(art -> art == null || art.getKnoten_id() == null);
        loaded.sort(Comparator.comparingInt(Art::getKnoten_id));

        Map<Integer, Art> index = new LinkedHashMap<>();
        for (Art art : loaded) {
            index.put(art.getKnoten_id(), art);
        }

        allArts = List.copyOf(loaded);
        byKnotenId = Map.copyOf(index);
        LOG.info("Loaded {} Arten for REST API.", allArts.size());
    }

    private List<Art> loadFromResource() {
        try (InputStream inputStream = ArtReadService.class.getClassLoader().getResourceAsStream("alleArten.obj")) {
            if (inputStream == null) {
                return null;
            }
            return EinObjektExporter.importArten(inputStream);
        } catch (Exception e) {
            LOG.warn("Could not load data from classpath resource 'alleArten.obj'.", e);
            return null;
        }
    }

    public Optional<Art> getByKnotenId(int knotenId) {
        return Optional.ofNullable(byKnotenId.get(knotenId));
    }

    public SearchResult search(String name, String gruppe, String regelwerk, String anhang, int limit, int offset) {
        int sanitizedLimit = Math.max(1, Math.min(limit, 1000));
        int sanitizedOffset = Math.max(0, offset);

        List<Art> filtered = new ArrayList<>();
        for (Art art : allArts) {
            if (!matchesName(art, name)) {
                continue;
            }
            if (!containsIgnoreCase(art.getGruppe(), gruppe)) {
                continue;
            }
            if (!matchesRegelwerk(art, regelwerk)) {
                continue;
            }
            if (!matchesAnhang(art, anhang)) {
                continue;
            }
            filtered.add(art);
        }

        int fromIndex = Math.min(sanitizedOffset, filtered.size());
        int toIndex = Math.min(fromIndex + sanitizedLimit, filtered.size());
        List<Art> page = filtered.subList(fromIndex, toIndex);

        return new SearchResult(page, filtered.size(), sanitizedLimit, sanitizedOffset);
    }

    private boolean matchesName(Art art, String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        if (containsIgnoreCase(art.getWissenschaftlicherName(), name)) {
            return true;
        }
        if (containsIgnoreCase(art.getGueltigerName2(), name)) {
            return true;
        }
        if (containsIgnoreCase(art.getDeutscherName(), name)) {
            return true;
        }
        if (containsIgnoreCase(art.getEnglischerName(), name)) {
            return true;
        }
        if (art.getSynonyme() != null) {
            for (String synonym : art.getSynonyme()) {
                if (containsIgnoreCase(synonym, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesRegelwerk(Art art, String regelwerk) {
        if (regelwerk == null || regelwerk.isBlank()) {
            return true;
        }
        if (art.getRegelwerke() == null) {
            return false;
        }
        for (Regelwerk rw : art.getRegelwerke()) {
            if (containsIgnoreCase(rw.getName(), regelwerk)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAnhang(Art art, String anhang) {
        if (anhang == null || anhang.isBlank()) {
            return true;
        }
        if (art.getRegelwerke() == null) {
            return false;
        }
        for (Regelwerk rw : art.getRegelwerke()) {
            if (rw.getAnhaenge() == null) {
                continue;
            }
            for (Anhang currentAnhang : rw.getAnhaenge()) {
                if (containsIgnoreCase(currentAnhang.getName(), anhang)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsIgnoreCase(String value, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    public record SearchResult(List<Art> items, int total, int limit, int offset) {
    }
}
