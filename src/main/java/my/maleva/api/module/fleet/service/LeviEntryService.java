package my.maleva.api.module.fleet.service;

/**
 * Levi entries. Replaces the legacy {@code LeviEntryServices} behind the
 * {@code /LeviEntry/*} MVC actions.
 *
 * The contract lives on {@link PassEntryService}; this exists so the levi bean
 * can be injected without colliding with the auto pass one.
 */
public interface LeviEntryService extends PassEntryService {
}
