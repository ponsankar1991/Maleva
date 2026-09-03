package my.maleva.api.module.ai.common;

import my.maleva.api.module.supplier.entity.Supplier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Matches the supplier name printed on a document to the supplier master.
 * Registration / GST / SST / TIN numbers win outright; otherwise names are
 * compared after stripping legal suffixes (SDN BHD, BERHAD, PLT...).
 */
public final class SupplierMatcher {

    public record Match(Supplier supplier, double score) {
    }

    public static final double ACCEPT_SCORE = 0.8;
    public static final double AMBIGUITY_GAP = 0.05;

    private static final Set<String> LEGAL_SUFFIXES = Set.of(
            "SDN", "BHD", "BERHAD", "PLT", "LLP", "LTD", "LIMITED", "PTE", "INC", "CO", "M", "THE");

    private SupplierMatcher() {
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String upper = name.toUpperCase(Locale.ROOT).replace("&", " AND ");
        String cleaned = upper.replaceAll("[^A-Z0-9 ]", " ");
        StringBuilder sb = new StringBuilder();
        for (String token : cleaned.trim().split("\\s+")) {
            if (token.isEmpty() || LEGAL_SUFFIXES.contains(token)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static String normalizeId(String id) {
        return id == null ? "" : id.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    public static double nameScore(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        if (a.equals(b)) {
            return 0.98;
        }
        if (a.length() >= 4 && b.length() >= 4 && (a.contains(b) || b.contains(a))) {
            return 0.85;
        }
        Set<String> ta = new HashSet<>(List.of(a.split(" ")));
        Set<String> tb = new HashSet<>(List.of(b.split(" ")));
        Set<String> union = new HashSet<>(ta);
        union.addAll(tb);
        ta.retainAll(tb);
        if (union.isEmpty()) {
            return 0;
        }
        return 0.8 * ta.size() / union.size();
    }

    public static List<Match> rank(SupplierHint hint, List<Supplier> suppliers) {
        String name = normalize(hint == null ? null : hint.name());
        Set<String> ids = new LinkedHashSet<>();
        if (hint != null) {
            for (String id : new String[]{hint.registrationNo(), hint.gstNo(), hint.sstNo(), hint.tinNo()}) {
                String normalized = normalizeId(id);
                if (normalized.length() >= 5) {
                    ids.add(normalized);
                }
            }
        }
        List<Match> matches = new ArrayList<>();
        for (Supplier supplier : suppliers) {
            if (supplier == null) {
                continue;
            }
            double score = nameScore(name, normalize(supplier.getSupplierName()));
            if (!ids.isEmpty()) {
                for (String candidate : new String[]{supplier.getRegistrationNo(), supplier.getGstNo(),
                        supplier.getSstNo(), supplier.getTinNo()}) {
                    String normalized = normalizeId(candidate);
                    if (normalized.length() >= 5 && ids.contains(normalized)) {
                        score = 1.0;
                        break;
                    }
                }
            }
            if (score > 0) {
                matches.add(new Match(supplier, Math.round(score * 100) / 100.0));
            }
        }
        matches.sort(Comparator.comparingDouble(Match::score).reversed()
                .thenComparing(m -> String.valueOf(m.supplier().getSupplierName())));
        return matches;
    }

    /** The top match when it is confident and not tied with the runner-up. */
    public static Optional<Match> best(List<Match> ranked) {
        if (ranked.isEmpty()) {
            return Optional.empty();
        }
        Match top = ranked.get(0);
        if (top.score() < ACCEPT_SCORE) {
            return Optional.empty();
        }
        if (ranked.size() > 1) {
            Match second = ranked.get(1);
            if (second.score() >= ACCEPT_SCORE && top.score() - second.score() < AMBIGUITY_GAP) {
                return Optional.empty();
            }
        }
        return Optional.of(top);
    }
}
