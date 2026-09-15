# ADR-001: Save suppliers in Java instead of calling SP_Supplier

**Status:** Accepted
**Date:** 2026-09-15
**Deciders:** Maleva migration owner (product/dev lead)

## Context

The Supplier Master screen (`AddSupplier.js` → `SupplierMasterController.InsertSupplier`)
saved through the stored procedure `SP_Supplier`, then pushed the supplier to QNE.
The React + Spring port had to reproduce that behaviour. Forces at play:

- **The .NET screen is still live** against the same tables. A supplier written by
  the new stack must be indistinguishable from one the procedure wrote: the ledger
  account row under SUPPLIERS/SUP (`SUP-n`), the `SU` + 9-digit number, and the
  procedure's per-column `UPPER(ISNULL(...))`, which differs between insert and edit.
- **The legacy call path damaged data.** `InsertSupplier` built the `EXEC` by string
  concatenation and first ran `Replace("'", "")` and `Replace("null", "\"\"")` over
  the JSON, so `O'NEILL` was stored as `ONEILL`. The procedure's master-check message
  (`'...id' + @SymbolRefid`) raised a conversion error instead of the message.
- **The first Spring version was wrong.** The generic JPA create hardcoded
  `AccountRefid = 1` and `CNumber = 1`, and the React page never called the server.
- **QNE is not transactional.** The push is an HTTP call to an external ledger that
  cannot be rolled back, and legacy only pushed after the procedure committed.
- The team knows Java well and reviews SQL comfortably; the owner asked explicitly
  that the save "not use the SP" and "not miss any code".

## Decision

1. **`SupplierWriter` runs the procedure's own statements from Java**, through
   `NamedParameterJdbcTemplate` inside the service transaction. Each SQL constant is
   the matching procedure statement with `@Variables` bound as parameters, so casing,
   `ISNULL`, `GETDATE()`, `SUSER_NAME()` and the column lists are unchanged, and the
   columns the procedure omits keep their database defaults.
2. **The QNE push runs after commit, outside the transaction**
   (`SupplierQneService.pushSaved`), orchestrated by the controller: save → push →
   re-read. It sends the values as typed (legacy did), only when the supplier has no
   QNE code, and returns an explicit outcome (`PUSHED / ALREADY_IN_QNE / DISABLED /
   FAILED`) beside the saved row instead of replacing a successful save with an error.
3. **Parity is proven by a test, not by review.** `SupplierSaveParityIT` saves the
   same supplier through `SP_Supplier` and through `SupplierWriter` in one rolled-back
   transaction on MalevanewDemo and compares every column of both tables.

## Options Considered

### Option A: Keep calling SP_Supplier from Spring

| Dimension | Assessment |
|-----------|------------|
| Complexity | Low — one `EXEC` with a JSON parameter |
| Cost | None now; logic stays split between C#-era SQL and Java |
| Scalability | Fine for master-data volumes |
| Team familiarity | Medium — T-SQL `OPENJSON` loop, temp table, nested TRAN |

**Pros:** Guaranteed identical to the .NET screen; no port to verify.
**Cons:** The rules stay in a procedure nobody versions in this repo (it was not on
disk); errors come back as a `Result/Msg` row, including the conversion-error bug;
the nested `BEGIN/ROLLBACK TRAN` rolls back a caller's whole transaction on any
failure; cannot be unit tested; the owner asked not to use it.

### Option B: JPA entity save (the first Spring version, corrected)

| Dimension | Assessment |
|-----------|------------|
| Complexity | Medium — Java upper-casing, entity mapping |
| Cost | Low |
| Scalability | Fine |
| Team familiarity | High |

**Pros:** Idiomatic Spring; easy to unit test with mocks.
**Cons:** Hibernate writes every mapped column, so `NULL` lands in OpenBalance,
QNECode and QNEId where the procedure left the default; Java `toUpperCase` is not
SQL Server's collation `UPPER`; a loaded entity can shadow JDBC writes in the same
persistence context. Faithful parity is possible but easy to drift from silently.

### Option C: Procedure statements executed from Java (chosen)

| Dimension | Assessment |
|-----------|------------|
| Complexity | Medium — ~10 SQL constants, readable beside the procedure |
| Cost | Low |
| Scalability | Fine |
| Team familiarity | High (Java) / Medium (SQL) |

**Pros:** Same SQL semantics as the procedure (collation, defaults, `SUSER_NAME()`);
bound parameters fix the apostrophe and `"null"` damage; clear exceptions for
master-check failures; runs in the Spring transaction; provable by a parity test.
**Cons:** Two copies of the rules exist while the .NET screen lives — a change to
`SP_Supplier` must be mirrored; SQL strings are only checked at runtime (covered by
the integration tests).

## Trade-off Analysis

The deciding forces are **data parity with a still-live system** and **the owner's
instruction not to call the procedure**. Option A wins on parity but fails the
instruction and keeps the known faults. Option B is the most idiomatic but its
defaults drift is exactly the kind of difference nobody notices until the .NET
screen and the new one disagree on a row. Option C keeps SQL Server doing the
casing and defaults — so parity holds by construction — while moving control flow,
validation and error handling into testable Java. Its one real cost, the duplicated
rules, is paid down by `SupplierSaveParityIT`, which fails the moment the two
diverge, and disappears when the .NET screen is retired.

For QNE, pushing inside the transaction would hold a database transaction open
across a network call of unbounded length and could leave a QNE supplier pointing at
a rolled-back row. After-commit keeps the local save authoritative, matching legacy
order; the outcome object keeps a QNE refusal from being mistaken for a failed save,
which in legacy led users to press Save again and create a duplicate supplier.

## Consequences

- **Easier:** reading and changing supplier rules in one place; clear error
  messages; unit and integration testing; a QNE refusal no longer produces duplicates
  (the screen reopens the saved supplier and UPDATE retries the push).
- **Harder:** any change to `SP_Supplier` while .NET is live must be mirrored in
  `SupplierWriter` — run `SupplierSaveParityIT` after touching either.
- **Deliberate divergences:** a company with no SUPPLIERS/SUP group is refused (the
  procedure created an account with a NULL parent); an edit with no Active flag keeps
  the stored value; a crashed QNE push is reported as FAILED (legacy said "created").
- **Revisit:** the `MAX(CNumber)+1` and sibling-count numbering race (inherited, needs
  a unique index + retry); whether QNE should receive supplier updates (legacy built
  but never sent them); removing the duplication once the .NET screen is retired.

## Action Items

1. [x] `SupplierWriter` with procedure-equivalent SQL; unit tests on SQL and bindings
2. [x] `SupplierQneService.pushSaved` + controller orchestration; unit tests on push rules
3. [x] `SupplierSaveParityIT` and `SupplierServiceIT` against MalevanewDemo (QNE off)
4. [x] React form sends the legacy defaults (`''` text, `0` for unchosen combos)
5. [ ] Verify a save end-to-end on the running app, including one real QNE push
6. [ ] Add `SupplierSaveParityIT` to the pre-deploy checklist while .NET is live
7. [ ] Decide on a unique index for `(CompanyRefId, CNumber)` on Supplier
