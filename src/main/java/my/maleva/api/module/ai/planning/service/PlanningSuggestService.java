package my.maleva.api.module.ai.planning.service;

import my.maleva.api.module.ai.planning.dto.PlanningSuggestFeedbackRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse;

public interface PlanningSuggestService {

    /**
     * Proposes a truck and driver for each planning row from the company's
     * planning history and the day's existing assignments, with warnings for
     * conflicts on rows that are already filled.
     */
    PlanningSuggestResponse suggest(PlanningSuggestRequest request);

    /** Records what the planner finally saved for rows that received a suggestion. Returns rows recorded. */
    int feedback(PlanningSuggestFeedbackRequest request);
}
