package my.maleva.api.module.ai.billextraction.service.impl;

import my.maleva.api.module.ai.billextraction.dto.ExtractedBill;
import my.maleva.api.module.supplier.entity.Supplier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierMatcherTest {

    private static Supplier supplier(int id, String name) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setSupplierName(name);
        return s;
    }

    @Test
    void normalizeDropsLegalSuffixesAndPunctuation() {
        assertThat(SupplierMatcher.normalize("Petronas Dagangan Berhad")).isEqualTo("PETRONAS DAGANGAN");
        assertThat(SupplierMatcher.normalize("SHELL MALAYSIA TRADING SDN. BHD.")).isEqualTo("SHELL MALAYSIA TRADING");
        assertThat(SupplierMatcher.normalize("A & B Logistics (M) Sdn Bhd")).isEqualTo("A AND B LOGISTICS");
    }

    @Test
    void exactNormalizedNameIsAConfidentMatch() {
        List<Supplier> suppliers = List.of(supplier(7, "PETRONAS DAGANGAN BHD"), supplier(8, "SHELL MALAYSIA TRADING SDN BHD"));
        ExtractedBill.ExtractedSupplier extracted = ExtractedBill.ExtractedSupplier.builder().name("Petronas Dagangan Berhad").build();

        List<SupplierMatcher.Match> ranked = SupplierMatcher.rank(extracted, suppliers);
        Optional<SupplierMatcher.Match> best = SupplierMatcher.best(ranked);

        assertThat(best).isPresent();
        assertThat(best.get().supplier().getId()).isEqualTo(7);
        assertThat(best.get().score()).isGreaterThanOrEqualTo(0.9);
    }

    @Test
    void containmentIsAccepted() {
        List<Supplier> suppliers = List.of(supplier(8, "SHELL MALAYSIA TRADING SDN BHD"));
        ExtractedBill.ExtractedSupplier extracted = ExtractedBill.ExtractedSupplier.builder().name("Shell Malaysia Trading Sdn Bhd - Klang").build();

        Optional<SupplierMatcher.Match> best = SupplierMatcher.best(SupplierMatcher.rank(extracted, suppliers));
        assertThat(best).isPresent();
        assertThat(best.get().supplier().getId()).isEqualTo(8);
    }

    @Test
    void registrationNumberWinsOverADifferentName() {
        Supplier byReg = supplier(9, "PDB TRADING");
        byReg.setRegistrationNo("198201004309 (88222-D)");
        List<Supplier> suppliers = List.of(supplier(7, "PETRONAS DAGANGAN BHD"), byReg);
        ExtractedBill.ExtractedSupplier extracted = ExtractedBill.ExtractedSupplier.builder()
                .name("Some Other Name").registrationNo("198201004309(88222-D)").build();

        Optional<SupplierMatcher.Match> best = SupplierMatcher.best(SupplierMatcher.rank(extracted, suppliers));
        assertThat(best).isPresent();
        assertThat(best.get().supplier().getId()).isEqualTo(9);
        assertThat(best.get().score()).isEqualTo(1.0);
    }

    @Test
    void tiedCandidatesAreAmbiguous() {
        List<Supplier> suppliers = List.of(supplier(1, "ABC SDN BHD"), supplier(2, "ABC BHD"));
        ExtractedBill.ExtractedSupplier extracted = ExtractedBill.ExtractedSupplier.builder().name("ABC").build();

        List<SupplierMatcher.Match> ranked = SupplierMatcher.rank(extracted, suppliers);
        assertThat(ranked).hasSize(2);
        assertThat(SupplierMatcher.best(ranked)).isEmpty();
    }

    @Test
    void weakOverlapIsOnlyACandidate() {
        List<Supplier> suppliers = List.of(supplier(1, "MALAYSIA AIRPORTS HOLDINGS BHD"));
        ExtractedBill.ExtractedSupplier extracted = ExtractedBill.ExtractedSupplier.builder().name("Malaysia Ports Authority").build();

        List<SupplierMatcher.Match> ranked = SupplierMatcher.rank(extracted, suppliers);
        assertThat(ranked).hasSize(1);
        assertThat(ranked.get(0).score()).isLessThan(SupplierMatcher.ACCEPT_SCORE);
        assertThat(SupplierMatcher.best(ranked)).isEmpty();
    }

    @Test
    void blankNameMatchesNothing() {
        assertThat(SupplierMatcher.rank(new ExtractedBill.ExtractedSupplier(), List.of(supplier(1, "X")))).isEmpty();
        assertThat(SupplierMatcher.rank(null, List.of(supplier(1, "X")))).isEmpty();
    }
}
