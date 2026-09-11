package my.maleva.api.module.customerstatement.print;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

/**
 * Counts the statement lines actually placed on the page for the current
 * customer, into the report variable {@code printedLines}.
 *
 * <p>Why a scriptlet: the column headings must appear on a continuation
 * page that carries lines, and must not appear on a page that carries only
 * the totals block (the footer is pinned to the page bottom, so when it does
 * not fit under the last line it moves to a page of its own). Jasper's own
 * counters cannot tell the two apart — on both pages {@code PAGE_COUNT} is 0
 * and the group count already includes the record about to be printed —
 * but {@code afterDetailEval} fires only once a line's band has been laid
 * down, so {@code printedLines < lineCount} is exactly "lines still to come".
 */
public class StatementLinesScriptlet extends JRDefaultScriptlet {

    static final String PRINTED_LINES = "printedLines";

    private int printed;

    @Override
    public void beforeGroupInit(String groupName) throws JRScriptletException {
        printed = 0;
        setVariableValue(PRINTED_LINES, 0);
    }

    @Override
    public void afterDetailEval() throws JRScriptletException {
        printed++;
        setVariableValue(PRINTED_LINES, printed);
    }
}
