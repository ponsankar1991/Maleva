package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.fleet.service.AutoPassEntryService;
import my.maleva.api.module.fleet.service.PassEntryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auto pass entries, replacing the legacy {@code /AutoPassEntry/*} MVC actions.
 * The routes are declared on {@link AbstractPassEntryController}.
 *
 * The screen files its attachments through {@code /api/attachments} under the
 * {@code AutoPassEntry} folder. Unlike the levi screen it used the legacy
 * {@code /Common/UploadFile} action, which also wrote the joined paths back to
 * {@code AutoPassEntry.FilePath} - so the React form passes that table through
 * as the attachment API's {@code filePathTable}.
 */
@RestController
@RequestMapping("/api/auto-pass-entries")
@Validated
@PermitAll
public class AutoPassEntryController extends AbstractPassEntryController {

    private final AutoPassEntryService service;

    public AutoPassEntryController(AutoPassEntryService service) {
        this.service = service;
    }

    @Override
    protected PassEntryService service() {
        return service;
    }

    @Override
    protected String documentLabel() {
        return "Auto pass entry";
    }
}
