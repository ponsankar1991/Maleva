## Context

The Truck Order Calendar (`/TruckMaster/TruckOrderTimetable`) was ported on 2026-09-01 to `TruckOrderController` (`/api/truck-orders`) and `features/truck-order`. Today:

- An order is one row in `TruckOrderMaster`: company, **one own truck** (`TruckRefId NOT NULL`), a whole-day `OrderDate` (SQL `date`), status and remarks.
- `TruckOrderServiceImpl.save` rejects a second live order on the same truck and day (`countClashes`), with the message *"This truck is already booked on the selected date."*
- The dialog lists every orderable truck (`TruckMaster.OrderableTruck = 1`, backfilled to the 14 plates the legacy JS hardcoded) and warns only after a truck is picked.
- `db/sql/TRUCK_ORDER_INDEXES.sql` defines a filtered UNIQUE index on (company, truck, day) where `Active = 1`. **It has not been applied yet.**

Findings from the MalevanewDemo planning data (read-only, 2026-09-15) that shape this design:

- Trucks carry several jobs a day: of about 3,560 truck-days in 12 months, 1,540 had 2 or more jobs, up to 15. Some plan rows are duplicates, so the counts run high.
- Around half of sale orders give no truck size, and the size asked for is often not the truck used. For example, QD 7151 (a 20 ft box) carried a "40 FT TRUCK" job. Size is a filter the dispatcher chooses, not a rule the system enforces.
- `TruckMaster.TruckType` is free text with typos (`4O FT BOX TRUCK` uses the letter O), and 26 active trucks have no type. Other code reads it (`getTruckComboByType`, the legacy combos).
- `TruckMaster.MalevaTruck` already separates own trucks (1) from subcontractor trucks (0). `RTIMaster.OutsideTruck` records outside trucks as free text.

House constraints:

- **Row counts:** the pool runs `SET NOCOUNT ON`, so JDBC UPDATE counts are -1. No affected-row checks.
- **Transactions:** no catch-and-continue inside a `@Transactional` save.
- **Database access:** schema and data scripts are run by the user; Claude does not execute SQL against the Maleva databases.
- **Request parameters:** camelCase; PascalCase binds to nothing.

## Goals / Non-Goals

**Goals:**
- At order entry, show which own trucks are free on the chosen day, optionally narrowed to one size class.
- When none are free, make the dispatcher choose explicitly: Share, Outside truck, or Cancel.
- Keep the "one own order per truck per day" rule, but let a deliberate Share bypass it.
- Show Trucks / Taken / Free per day on every calendar view.
- Make the orderable fleet match the trucks planning actually uses.

**Non-Goals:**
- No weight, pallet, CBM or trip capacity maths. Share is the dispatcher's judgement.
- No automatic truck suggestion. The AI planning suggest service is untouched.
- No linking to Sale Orders, Planning or RTI. The unused `saleordermasterrefid` column stays unmapped.
- No change to the legacy .NET page or `SP_TruckOrderMaster`.
- No outside-truck cost or supplier bill.
- A bigger own truck is not offered automatically when the chosen size is full. The user can clear the size filter.

## Decisions

### D1. A `BookingType` column on the order: `OWN` | `SHARED` | `OUTSIDE`

`TruckOrderMaster.BookingType varchar(10) NOT NULL DEFAULT 'OWN'`. A NOT NULL column added with a default fills existing rows, so every current order becomes `OWN` with no backfill. The value is mirrored by a Java enum `TruckBookingType` and a TS constant, kept in step, as `TruckOrderStatus` is today.

- **Alternative:** a link table `SharedWithOrderId` pointing a shared order at its host order. Rejected, because deleting or moving the host would orphan or silently move the shared order. A shared order only needs to know its truck and day.
- **Alternative:** book outside orders against the existing `OUTSIDE TRUCK` placeholder row in TruckMaster (`MalevaTruck = 0`). Rejected. It loses which outside truck and supplier were used, it would collide with the one-order-per-day index, and it would inflate truck counts.

### D2. `OUTSIDE` orders carry no own truck

- `TruckRefId` becomes nullable.
- New `OutsideTruckName varchar(100)` (required for OUTSIDE) and `OutsideSupplierName varchar(200)` (optional).
- The server enforces the combinations:
  - OWN and SHARED: truck required, outside fields cleared.
  - OUTSIDE: truck cleared, outside truck name required.
- Free text follows `RTIMaster.OutsideTruck`. A supplier-master dropdown can come later without a schema change.

### D3. Size class is a new column, not a rewrite of `TruckType`

- `TruckMaster.SizeClass varchar(10) NULL` holds one of `1TON`, `3TON`, `5TON`, `10TON`, `20FT`, `40FT`, `SPECIAL`. Enum `TruckSizeClass`, with display labels `1 TON`, `3 TON`, `5 TON`, `10 TON`, `20 FT`, `40 FT`, `SPECIAL`.
- `TruckType` stays as is, because other screens filter on its exact text.
- The order records the size the dispatcher searched for: `TruckOrderMaster.TruckSizeClass varchar(10) NULL`. It is kept for reporting only; it never restricts which truck can be saved.
- A 4 TON truck maps to `3TON`, the nearest class that dispatchers treat the same way. `LOW BED` and `LORRY RIGID` map to `SPECIAL`.
- Trucks with NULL `SizeClass` appear only under "Any size".

### D4. Availability is computed on the server, in one place

**New endpoint:** `GET /api/truck-orders/availability?companyRefId&orderDate&sizeClass?&excludeId?`

```
{ orderDate, sizeClass,
  totalTrucks, takenCount, freeCount,
  freeTrucks:  [{ id, truckName, sizeClass }],
  takenTrucks: [{ id, truckName, sizeClass,
                  orders: [{ id, cNumberDisplay, bookingType, status, remarks }] }],
  outsideOrders: [{ id, cNumberDisplay, outsideTruckName, outsideSupplierName }] }
```

**Counting rules**, shared by the endpoint and the calendar counters:
- **Trucks:** orderable own trucks (`Active = 1 AND OrderableTruck = 1 AND MalevaTruck = 1`), narrowed to `sizeClass` when one is given.
- **Taken:** those trucks with at least one live `OWN` or `SHARED` order that day, excluding `excludeId`, so an edit does not make its own truck look taken.
- **Free:** Trucks minus Taken.
- **Outside orders:** listed, never counted.

**Calendar counters:** `TruckOrderCalendarResponse` gains `days: [{ date, totalTrucks, takenTrucks, freeTrucks, outsideOrders }]` for every date from `fromDate` to `toDate`, all sizes. It is only filled when both dates are present and the range is 62 days or fewer, which covers the month and week views.

**Alternative:** compute the counters in the browser from the loaded orders and trucks, as `DayDetailPanel` does today. Rejected. The same rule would live in two languages, and the existing response already computes its totals on the server so the footer cannot disagree with the grid.

The existing `/clash` endpoint is kept, narrowed to OWN/SHARED bookings on the truck, and used only in edit mode.

**Search behaviour:** the list API and `TruckOrderSpecification` return all three booking types. The truck filter matches only OWN/SHARED rows, since OUTSIDE rows have no truck.

### D5. Save rules per booking type

| Type | Truck | Rule at save | Error |
|---|---|---|---|
| `OWN` | own orderable truck | no other live OWN or SHARED order on that truck and day | *"No truck available: {truck} is already booked on {date}."* |
| `SHARED` | own orderable truck | at least one other live OWN or SHARED order on that truck and day | *"{truck} is free on {date} — book it as own."* |
| `OUTSIDE` | none | `outsideTruckName` not blank | *"Outside truck name is required."* |

- The existing checks stay: truck active and belonging to the company, date required, status in the enum.
- An edit may change the booking type; the rules above are re-checked.
- Deleting a host order leaves its shared orders as they are. The truck still counts as taken while any of them are live, so nothing is double-booked.

### D6. Concurrent OWN bookings are stopped by the database

- **Index:** `db/sql/TRUCK_ORDER_INDEXES.sql` is rewritten (it has never been applied). The unique index becomes (CompanyRefId, TruckRefId, OrderDate) `WHERE Active = 1 AND BookingType = 'OWN'`. Its pre-check query is filtered the same way.
- **Race:** two OWN saves that both pass `countClashes` are stopped by the index.
- **Error mapping:** the save uses `saveAndFlush`, so the violation is raised inside the service call. A module-scoped `@RestControllerAdvice(assignableTypes = TruckOrderController.class)` maps `DataIntegrityViolationException` naming `UX_TruckOrderMaster_Truck_Day_Active` to the D5 OWN message, with HTTP 409. This follows the existing `SaleOrderExceptionHandler` and `InventoryExceptionHandler` pattern.
- **No catch in the service:** catching inside the `@Transactional` method would leave the transaction rollback-only.

**Remaining gap:** an OWN save and a SHARED save racing on a free truck can both succeed. The result is a truck with one own order and one shared order, which is the state the dispatcher meant anyway. Accepted.

### D7. Dialog flow

Revised 2026-09-16 after the first build: the dispatcher asked to see **every truck for the day with the load it already carries**, not only the free ones, and to choose from that one list. Share is no longer a separate screen — it is what picking a loaded truck means.

```
 Order date ─┐
 Size [All ▼]┴─► GET availability ──► one dropdown, free trucks first:
                                        JQX 7151 · 40 FT — free
                                        BPR 7151 · 40 FT — 2 orders
                                        QD 7151  · 20 FT — 1 order
                                      │
              pick a truck ───────────┤─ no orders that day ──► Save as OWN
                                      └─ has orders ─► its orders are listed
                                                       ("saving adds to the same
                                                        truck") ──► Save as SHARED
              [Use an outside truck instead] ─► truck name + supplier ─► Save as OUTSIDE
              [Cancel] ─► close, nothing saved

 freeCount = 0 ─► red line above the dropdown: "No truck available. All N trucks
                  are booked on 20 Sep. Pick one below to share it, or use an
                  outside truck."
```

- **Why one list:** the dispatcher decides whether a job fits on a truck that is already loaded, and that decision needs the truck's current load in front of them. Two lists hid the loaded trucks behind a button.
- **The booking type is derived, never typed:** loaded truck → SHARED, empty truck → OWN, outside form → OUTSIDE. The server still enforces D5, so a truck that fills up between the read and the save is rejected.
- **Refetching:** availability is refetched with `staleTime: 0` whenever the date or size changes, and when the dialog opens.
- **Stale screen:** if a save fails with the OWN conflict (another user took the truck), the dialog refetches and shows the new state instead of only a toast.
- **Edit mode:** the dialog opens on the order's booking type and passes `excludeId`.
- **Unchanged fields:** read-only order number, status, remarks and Delete behave as today.

### D8. Calendar views

- **Monthly cell:** order count and the first trucks as today, plus `Free n/N`. The cell is red when `freeTrucks = 0` and `totalTrucks > 0`.
- **Weekly and Per Truck:**
  - Own truck rows stay. A cell with more than one order shows a `+n` badge.
  - A final **Outside** row lists outside orders per day.
  - The header of each day shows `Free n/N`.
- **Day panel:**
  - A **Type** column, and outside rows show the outside truck name.
  - The free-truck chips come from the server `days` entry and the availability call, not from browser arithmetic.
- **Summary footer:** unchanged (Total Orders, Booked Trucks).
- **Excel export:** gains the columns Type, Outside Truck and Supplier.

### D9. The fleet comes from planning history, via a script the user runs

New script `db/sql/TRUCK_ORDER_FLEET_FROM_PLANNING.sql`, in three steps:

1. **Schema:** add the columns from D1–D3 (guarded with `IF NOT EXISTS`) and make `TruckRefId` nullable.
2. **Preview (SELECT only):** list the active own trucks with a `PLANINGDetails.TruckRefid` job whose pickup date falls in the last 180 days, with their current `TruckType`, proposed `SizeClass` and job count.
3. **Apply:**
   - Set `OrderableTruck = 1` on exactly those trucks. Never set anything to 0, so choices made since the last backfill survive.
   - Set `SizeClass` from a `CASE` over trimmed, upper-cased `TruckType`. The patterns cover `40`, `4O`, `20`, `2O`, `TON`, `TONNER`, `LOW BED`, `RIGID`.
   - Print the orderable trucks whose `SizeClass` is still NULL, for the user to fix by hand.

The trucks seen in the 2026-09 planning sample include JQX, SP, BPR, QD, JWS, VIPS, JQV, JPM, VR, BNU and GM 7151, plus JYW7151 and JTY2282. The legacy list adds JTB, GOLD and UMS 7151. The script's preview is the authoritative list, not this note.

## Risks / Trade-offs

- **[Legacy .NET page]**
  - It INNER JOINs TruckMaster, so it will not show OUTSIDE orders.
  - `SP_TruckOrderMaster`'s SELECT-based clash check will refuse to re-save a SHARED order.
  - **Mitigation:** the React page serves the same route. Confirm the legacy page is no longer used before the migration (open question).
- **[Size class data quality]** A wrong or NULL `SizeClass` hides a truck from a size-filtered list. **Mitigation:** the script's review list; NULL trucks still appear under "Any size"; the default filter is "Any size".
- **[Share overuse]** There is no capacity maths, so a truck can be over-shared. **Mitigation:** the dialog lists each taken truck's current orders before sharing, and week/per-truck cells show the `+n` count. Capacity limits are a possible follow-up.
- **[Stale availability]** Two dispatchers can look at the same free truck. **Mitigation:** the save re-checks (D5), the unique index catches races (D6), and the dialog refetches on conflict.
- **[Scripts name MalevanewDemo]** The existing scripts start with `USE [MalevanewDemo]`. **Mitigation:** the new script uses no `USE` line; the header says to select the target database before running.
- **[Script order]** The backend maps the new columns, so it fails to start if deployed before step 1. **Mitigation:** the migration order below; the startup log shows the JPA mapping error clearly.
- **[`SET NOCOUNT ON`]** No logic relies on UPDATE row counts. Deletes still load and save the managed entity.

## Migration Plan

1. User runs step 1 (schema) of `TRUCK_ORDER_FLEET_FROM_PLANNING.sql` on the target database.
2. User runs step 2 (preview), checks the truck list and size classes, then runs step 3 (apply). Any NULL size classes are fixed by hand.
3. Deploy the backend (entity, DTOs, service rules, availability endpoint, exception advice).
4. Deploy the frontend (dialog flow, calendar counters, types).
5. User runs the rewritten `TRUCK_ORDER_INDEXES.sql`. Its duplicate check now counts OWN rows only.

**Rollback:**
- Revert the frontend and backend. The new columns are additive and harmless to the old code, except that OUTSIDE rows (NULL `TruckRefId`) would fail to map in the old entity.
- Before a full rollback, list OUTSIDE rows and either delete them (`Active = 2`) or assign them a truck.
- `OrderableTruck` values set by step 3 can be kept or reset from the step 2 preview output.

## Open Questions

1. **Legacy page:** is the .NET `/TruckMaster/TruckOrderTimetable` page still reachable by users in production, or fully replaced by React on IIS?
2. **Planning window:** is 180 days the right lookback for the fleet? Trucks idle longer than that would drop out of the preview; they can be marked by hand.
3. **4 TON trucks:** should they be their own class instead of `3TON`?
4. **Outside supplier:** free text for now; should it pick from Supplier Master later?
