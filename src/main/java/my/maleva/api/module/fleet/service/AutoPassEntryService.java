package my.maleva.api.module.fleet.service;

/**
 * Auto pass entries. Replaces the legacy {@code AutoPassEntryServices} behind
 * the {@code /AutoPassEntry/*} MVC actions.
 *
 * The contract lives on {@link PassEntryService}; this exists so the auto pass
 * bean can be injected without colliding with the levi one.
 */
public interface AutoPassEntryService extends PassEntryService {
}
