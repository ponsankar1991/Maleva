package my.maleva.api.module.rti.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The NOT NULL columns on RTIMaster carry database defaults, but Hibernate names
 * every column in its INSERT, so a null field is sent as an explicit NULL and the
 * default never applies — the insert is rejected instead.
 *
 * <p>The RTI screen hides this by computing all the amounts in the browser. Any
 * other caller — a plan batch, an import, a report — would hit it. The entity is
 * the right place to hold the table's contract, so these defaults live in
 * {@code @PrePersist} rather than in one caller.
 */
class RTIMasterDefaultsTest {

    /** Runs what JPA runs before an insert. */
    private static RTIMaster prepared(RTIMaster entity) {
        entity.onCreate();
        return entity;
    }

    @Test
    void fillsEveryNotNullColumnThatWasLeftEmpty() {
        RTIMaster entity = prepared(new RTIMaster());

        assertThat(entity.getAmount()).isEqualTo(0.0);
        assertThat(entity.getSleepingAmount()).isEqualTo(0.0);
        assertThat(entity.getPickupAmount()).isEqualTo(0.0);
        assertThat(entity.getDropAmount()).isEqualTo(0.0);
        assertThat(entity.getExitAmount()).isEqualTo(0);
        assertThat(entity.getSleeping()).isZero();
        assertThat(entity.getPickup()).isZero();
        assertThat(entity.getPickupCount()).isZero();
        assertThat(entity.getDropCount()).isZero();
        assertThat(entity.getAddDrop()).isZero();
        assertThat(entity.getExitYN()).isZero();
        assertThat(entity.getCreatedDate()).isNotNull();
        assertThat(entity.getModifiedDate()).isNotNull();
    }

    @Test
    void neverOverwritesAnAmountThatWasActuallyEntered() {
        RTIMaster entity = new RTIMaster();
        entity.setAmount(250.50);
        entity.setPickupAmount(30.0);
        entity.setExitAmount(80);
        entity.setSleeping(1);

        prepared(entity);

        assertThat(entity.getAmount()).isEqualTo(250.50);
        assertThat(entity.getPickupAmount()).isEqualTo(30.0);
        assertThat(entity.getExitAmount()).isEqualTo(80);
        assertThat(entity.getSleeping()).isEqualTo(1);
    }
}
