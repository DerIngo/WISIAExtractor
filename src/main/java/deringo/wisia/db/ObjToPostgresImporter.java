package deringo.wisia.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import deringo.wisia.art.Anhang;
import deringo.wisia.art.Art;
import deringo.wisia.art.Regelwerk;
import deringo.wisia.art.Unterschutzstellung;
import deringo.wisia.exporter.EinObjektExporter;

/**
 * Imports alleArten.obj into the PostgreSQL schema from db/sql/001_init_schema.sql.
 */
public class ObjToPostgresImporter {
    public static void main(String[] args) throws Exception {
        Config config = Config.load();
        ImportSource source = resolveSource(config.dataFile);
        if (source.arten == null || source.arten.isEmpty()) {
            throw new IllegalStateException("No art data loaded from: " + source.sourcePath);
        }

        System.out.printf(Locale.ROOT, "Loaded %d Arten from %s%n", source.arten.size(), source.sourcePath);

        try (Connection connection = DriverManager.getConnection(config.jdbcUrl, config.dbUser, config.dbPassword)) {
            long runId = createImportRun(connection, source, config);
            Counters counters = new Counters();

            try {
                connection.setAutoCommit(false);
                importData(connection, runId, source.arten, counters, config.importLimit);
                updateImportRunSuccess(connection, runId, counters);
                connection.commit();
                System.out.printf(Locale.ROOT, "Import run %d finished successfully.%n", runId);
            } catch (Exception e) {
                connection.rollback();
                connection.setAutoCommit(true);
                updateImportRunFailed(connection, runId, counters, e);
                throw e;
            }
        }
    }

    private static ImportSource resolveSource(String configuredPath) {
        List<String> candidates = new ArrayList<>();
        addCandidate(candidates, configuredPath);
        addCandidate(candidates, "files/export/alleArten.obj");
        addCandidate(candidates, "arten/alleArten.obj");

        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            Path path = Paths.get(candidate);
            if (!Files.exists(path)) {
                continue;
            }
            List<Art> arten = EinObjektExporter.importArten(candidate);
            if (arten != null) {
                return new ImportSource(candidate, checksum(path), arten);
            }
        }
        return new ImportSource(configuredPath, null, null);
    }

    private static void addCandidate(List<String> candidates, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!candidates.contains(value)) {
            candidates.add(value);
        }
    }

    private static String checksum(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(path);
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static long createImportRun(Connection connection, ImportSource source, Config config) throws SQLException {
        String sql = "insert into import_run(status, source_type, source_path, source_checksum, parser_version) "
                + "values (?, ?, ?, ?, ?) returning id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "RUNNING");
            ps.setString(2, "OBJ_FILE");
            ps.setString(3, source.sourcePath);
            ps.setString(4, source.checksum);
            ps.setString(5, config.parserVersion);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long runId = rs.getLong(1);
                System.out.printf(Locale.ROOT, "Created import_run %d%n", runId);
                return runId;
            }
        }
    }

    private static void importData(Connection connection, long runId, List<Art> arten, Counters counters, int importLimit)
            throws SQLException {
        String insertTaxon = "insert into taxon(import_run_id, knoten_id, wissenschaftlicher_name, gueltiger_name2, "
                + "gruppe, deutscher_name, englischer_name, ergaenzende_anmerkung) values (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertTaxonomie = "insert into taxon_taxonomie(import_run_id, knoten_id, position, element) "
                + "values (?, ?, ?, ?) on conflict do nothing";
        String insertSynonym = "insert into taxon_synonym(import_run_id, knoten_id, synonym) "
                + "values (?, ?, ?) on conflict do nothing";
        String upsertRegelwerk = "insert into regelwerk(import_run_id, name) values (?, ?) "
                + "on conflict (import_run_id, name) do update set name = excluded.name returning regelwerk_id";
        String insertTaxonRegelwerkName = "insert into taxon_regelwerk_name(import_run_id, knoten_id, regelwerk_id, name_im_regelwerk) "
                + "values (?, ?, ?, ?) on conflict do nothing";
        String upsertAnhang = "insert into anhang(import_run_id, regelwerk_id, name) values (?, ?, ?) "
                + "on conflict (import_run_id, regelwerk_id, name) do update set name = excluded.name returning anhang_id";
        String upsertTaxonAnhang = "insert into taxon_anhang(import_run_id, knoten_id, anhang_id, name_im_regelwerk) "
                + "values (?, ?, ?, ?) on conflict (import_run_id, knoten_id, anhang_id) do update "
                + "set name_im_regelwerk = excluded.name_im_regelwerk";
        String upsertFussnote = "insert into fussnote(import_run_id, fussnote_id, text) values (?, ?, ?) "
                + "on conflict (import_run_id, fussnote_id) do update set text = excluded.text";
        String insertTaxonAnhangFussnote = "insert into taxon_anhang_fussnote(import_run_id, knoten_id, anhang_id, fussnote_id) "
                + "values (?, ?, ?, ?) on conflict do nothing";
        String insertSchutzdetail = "insert into taxon_schutzdetail(import_run_id, knoten_id, position, unterschutzstellung, datum, bemerkung) "
                + "values (?, ?, ?, ?, ?, ?) on conflict do nothing";

        Map<String, Long> regelwerkCache = new LinkedHashMap<>();
        Map<String, Long> anhangCache = new LinkedHashMap<>();
        Set<String> fussnoteCache = new LinkedHashSet<>();
        int processed = 0;

        try (PreparedStatement psTaxon = connection.prepareStatement(insertTaxon);
                PreparedStatement psTaxonomie = connection.prepareStatement(insertTaxonomie);
                PreparedStatement psSynonym = connection.prepareStatement(insertSynonym);
                PreparedStatement psUpsertRegelwerk = connection.prepareStatement(upsertRegelwerk);
                PreparedStatement psTaxonRegelwerkName = connection.prepareStatement(insertTaxonRegelwerkName);
                PreparedStatement psUpsertAnhang = connection.prepareStatement(upsertAnhang);
                PreparedStatement psTaxonAnhang = connection.prepareStatement(upsertTaxonAnhang);
                PreparedStatement psUpsertFussnote = connection.prepareStatement(upsertFussnote);
                PreparedStatement psTaxonAnhangFussnote = connection.prepareStatement(insertTaxonAnhangFussnote);
                PreparedStatement psSchutzdetail = connection.prepareStatement(insertSchutzdetail)) {

            for (Art art : arten) {
                if (importLimit > 0 && processed >= importLimit) {
                    break;
                }
                if (art == null || art.getKnoten_id() == null) {
                    continue;
                }
                processed++;
                int knotenId = art.getKnoten_id();

                psTaxon.setLong(1, runId);
                psTaxon.setInt(2, knotenId);
                psTaxon.setString(3, art.getWissenschaftlicherName());
                psTaxon.setString(4, art.getGueltigerName2());
                psTaxon.setString(5, art.getGruppe());
                psTaxon.setString(6, art.getDeutscherName());
                psTaxon.setString(7, art.getEnglischerName());
                psTaxon.setString(8, art.getErgaenzendeAnmerkung());
                psTaxon.executeUpdate();
                counters.taxon++;

                if (art.getTaxonomie() != null) {
                    for (int i = 0; i < art.getTaxonomie().size(); i++) {
                        String element = art.getTaxonomie().get(i);
                        if (element == null) {
                            continue;
                        }
                        psTaxonomie.setLong(1, runId);
                        psTaxonomie.setInt(2, knotenId);
                        psTaxonomie.setInt(3, i);
                        psTaxonomie.setString(4, element);
                        counters.taxonomie += psTaxonomie.executeUpdate();
                    }
                }

                if (art.getSynonyme() != null) {
                    for (String synonym : art.getSynonyme()) {
                        if (synonym == null) {
                            continue;
                        }
                        psSynonym.setLong(1, runId);
                        psSynonym.setInt(2, knotenId);
                        psSynonym.setString(3, synonym);
                        counters.synonym += psSynonym.executeUpdate();
                    }
                }

                if (art.getRegelwerke() != null) {
                    for (Regelwerk regelwerk : art.getRegelwerke()) {
                        if (regelwerk == null || regelwerk.getName() == null) {
                            continue;
                        }
                        String regelwerkName = regelwerk.getName();
                        Long regelwerkId = regelwerkCache.get(regelwerkName);
                        if (regelwerkId == null) {
                            regelwerkId = upsertRegelwerk(psUpsertRegelwerk, runId, regelwerkName);
                            regelwerkCache.put(regelwerkName, regelwerkId);
                            counters.regelwerk++;
                        }

                        if (regelwerk.getNamenImRegelwerk() != null) {
                            for (String nameImRegelwerk : regelwerk.getNamenImRegelwerk()) {
                                if (nameImRegelwerk == null) {
                                    continue;
                                }
                                psTaxonRegelwerkName.setLong(1, runId);
                                psTaxonRegelwerkName.setInt(2, knotenId);
                                psTaxonRegelwerkName.setLong(3, regelwerkId);
                                psTaxonRegelwerkName.setString(4, nameImRegelwerk);
                                psTaxonRegelwerkName.executeUpdate();
                            }
                        }

                        if (regelwerk.getAnhaenge() != null) {
                            for (Anhang anhang : regelwerk.getAnhaenge()) {
                                if (anhang == null || anhang.getName() == null) {
                                    continue;
                                }
                                String anhangKey = regelwerkId + "|" + anhang.getName();
                                Long anhangId = anhangCache.get(anhangKey);
                                if (anhangId == null) {
                                    anhangId = upsertAnhang(psUpsertAnhang, runId, regelwerkId, anhang.getName());
                                    anhangCache.put(anhangKey, anhangId);
                                    counters.anhang++;
                                }

                                psTaxonAnhang.setLong(1, runId);
                                psTaxonAnhang.setInt(2, knotenId);
                                psTaxonAnhang.setLong(3, anhangId);
                                psTaxonAnhang.setString(4, null);
                                psTaxonAnhang.executeUpdate();

                                if (anhang.getFussnoten() != null) {
                                    for (deringo.wisia.art.Fussnote fussnote : anhang.getFussnoten()) {
                                        if (fussnote == null) {
                                            continue;
                                        }
                                        String fussnoteId = normalizeFussnoteId(fussnote.getId(), fussnote.getText());
                                        if (fussnoteId == null || fussnote.getText() == null) {
                                            continue;
                                        }

                                        if (!fussnoteCache.contains(fussnoteId)) {
                                            psUpsertFussnote.setLong(1, runId);
                                            psUpsertFussnote.setString(2, fussnoteId);
                                            psUpsertFussnote.setString(3, fussnote.getText());
                                            psUpsertFussnote.executeUpdate();
                                            fussnoteCache.add(fussnoteId);
                                            counters.fussnote++;
                                        }

                                        psTaxonAnhangFussnote.setLong(1, runId);
                                        psTaxonAnhangFussnote.setInt(2, knotenId);
                                        psTaxonAnhangFussnote.setLong(3, anhangId);
                                        psTaxonAnhangFussnote.setString(4, fussnoteId);
                                        psTaxonAnhangFussnote.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }

                if (art.getDetaillierteSchutzdaten() != null) {
                    for (int i = 0; i < art.getDetaillierteSchutzdaten().size(); i++) {
                        Unterschutzstellung schutz = art.getDetaillierteSchutzdaten().get(i);
                        if (schutz == null || schutz.getUnterschutzstellung() == null) {
                            continue;
                        }
                        psSchutzdetail.setLong(1, runId);
                        psSchutzdetail.setInt(2, knotenId);
                        psSchutzdetail.setInt(3, i);
                        psSchutzdetail.setString(4, schutz.getUnterschutzstellung());
                        psSchutzdetail.setDate(5, schutz.getDatum() == null ? null : Date.valueOf(schutz.getDatum()));
                        psSchutzdetail.setString(6, schutz.getBemerkung());
                        counters.schutzdetail += psSchutzdetail.executeUpdate();
                    }
                }

                if (processed % 1000 == 0) {
                    System.out.printf(Locale.ROOT, "Processed %d Arten%n", processed);
                }
            }
        }
    }

    private static long upsertRegelwerk(PreparedStatement ps, long runId, String name) throws SQLException {
        ps.setLong(1, runId);
        ps.setString(2, name);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static long upsertAnhang(PreparedStatement ps, long runId, long regelwerkId, String name) throws SQLException {
        ps.setLong(1, runId);
        ps.setLong(2, regelwerkId);
        ps.setString(3, name);
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static String normalizeFussnoteId(String id, String text) {
        if (id != null && !id.isBlank()) {
            return id;
        }
        if (text == null || text.isBlank()) {
            return null;
        }
        return "GEN_" + Integer.toUnsignedString(text.hashCode());
    }

    private static void updateImportRunSuccess(Connection connection, long runId, Counters counters) throws SQLException {
        String sql = "update import_run set finished_at = ?, status = ?, row_count_taxon = ?, row_count_synonym = ?, "
                + "row_count_taxonomie = ?, row_count_regelwerk = ?, row_count_anhang = ?, row_count_fussnote = ?, "
                + "row_count_schutzdetail = ?, error_count = 0, error_summary = null, stats_json = cast(? as jsonb) "
                + "where id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setString(2, "SUCCESS");
            ps.setInt(3, counters.taxon);
            ps.setInt(4, counters.synonym);
            ps.setInt(5, counters.taxonomie);
            ps.setInt(6, counters.regelwerk);
            ps.setInt(7, counters.anhang);
            ps.setInt(8, counters.fussnote);
            ps.setInt(9, counters.schutzdetail);
            ps.setString(10, counters.toJson());
            ps.setLong(11, runId);
            ps.executeUpdate();
        }
    }

    private static void updateImportRunFailed(Connection connection, long runId, Counters counters, Exception error)
            throws SQLException {
        String sql = "update import_run set finished_at = ?, status = ?, error_count = 1, error_summary = ?, "
                + "row_count_taxon = ?, row_count_synonym = ?, row_count_taxonomie = ?, row_count_regelwerk = ?, "
                + "row_count_anhang = ?, row_count_fussnote = ?, row_count_schutzdetail = ?, "
                + "stats_json = cast(? as jsonb) where id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setString(2, "FAILED");
            ps.setString(3, trim(error.toString(), 4000));
            ps.setInt(4, counters.taxon);
            ps.setInt(5, counters.synonym);
            ps.setInt(6, counters.taxonomie);
            ps.setInt(7, counters.regelwerk);
            ps.setInt(8, counters.anhang);
            ps.setInt(9, counters.fussnote);
            ps.setInt(10, counters.schutzdetail);
            ps.setString(11, counters.toJson());
            ps.setLong(12, runId);
            ps.executeUpdate();
        }
    }

    private static String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record Config(String dataFile, String jdbcUrl, String dbUser, String dbPassword, String parserVersion,
            int importLimit) {
        static Config load() {
            String dataFile = value("wisia.data.file", "WISIA_DATA_FILE", "files/export/alleArten.obj");
            String jdbcUrl = value("wisia.db.url", "WISIA_DB_URL", "jdbc:postgresql://localhost:5432/wisia");
            String dbUser = value("wisia.db.user", "WISIA_DB_USER", "wisia");
            String dbPassword = value("wisia.db.password", "WISIA_DB_PASSWORD", "wisia");
            String parserVersion = value("wisia.import.parserVersion", "WISIA_IMPORT_PARSER_VERSION",
                    "obj-importer-v1");
            int importLimit = Integer.parseInt(value("wisia.import.limit", "WISIA_IMPORT_LIMIT", "0"));
            return new Config(dataFile, jdbcUrl, dbUser, dbPassword, parserVersion, importLimit);
        }

        private static String value(String sysProp, String envVar, String defaultValue) {
            String fromSysProp = System.getProperty(sysProp);
            if (fromSysProp != null && !fromSysProp.isBlank()) {
                return fromSysProp;
            }
            String fromEnv = System.getenv(envVar);
            if (fromEnv != null && !fromEnv.isBlank()) {
                return fromEnv;
            }
            return defaultValue;
        }
    }

    private static class Counters {
        int taxon;
        int synonym;
        int taxonomie;
        int regelwerk;
        int anhang;
        int fussnote;
        int schutzdetail;

        String toJson() {
            return String.format(Locale.ROOT,
                    "{\"taxon\":%d,\"synonym\":%d,\"taxonomie\":%d,\"regelwerk\":%d,\"anhang\":%d,\"fussnote\":%d,\"schutzdetail\":%d}",
                    taxon, synonym, taxonomie, regelwerk, anhang, fussnote, schutzdetail);
        }
    }

    private record ImportSource(String sourcePath, String checksum, List<Art> arten) {
    }
}
