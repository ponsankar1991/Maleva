package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.fleet.service.LeviEntryService;
import my.maleva.api.module.fleet.service.PassEntryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Levi entries, replacing the legacy {@code /LeviEntry/*} MVC actions.
 * The routes are declared on {@link AbstractPassEntryController}.
 *
 * The screen files its attachments through {@code /api/attachments} under the
 * {@code LeviEntry} folder, which is where the legacy
 * {@code /Common/UploadFile2} calls put them.
 */
@RestController
@RequestMapping("/api/levi-entries")
@Validated
@PermitAll
public class LeviEntryController extends AbstractPassEntryController {

    private final LeviEntryService service;

    public LeviEntryController(LeviEntryService service) {
        this.service = service;
    }

    @Override
    protected PassEntryService service() {
        return service;
    }

    @Override
    protected String documentLabel() {
        return "Levi entry";
    }
}
