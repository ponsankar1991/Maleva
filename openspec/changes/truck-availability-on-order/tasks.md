## 1. Database scripts (written here, run by the user)

- [x] 1.1 Create `db/sql/TRUCK_ORDER_FLEET_FROM_PLANNING.sql` step 1 (schema, `IF NOT EXISTS` guarded, no `USE` line):
  - add `TruckOrderMaster.BookingType varchar(10) NOT NULL DEFAULT 'OWN'`, `OutsideTruckName varchar(100) NULL`, `OutsideSupplierName varchar(200) NULL`, `TruckSizeClass varchar(10) NULL`
  - make `TruckOrderMaster.TruckRefId` nullable
  - add `TruckMaster.SizeClass varchar(10) NULL`
- [x] 1.2 Add step 2 (SELECT-only preview): active `MalevaTruck = 1` trucks with a `PLANINGDetails.TruckRefid` job picked up in the last 180 days, with `TruckName`, `TruckType`, proposed `SizeClass` (CASE over trimmed upper `TruckType`, covering `40`/`4O`, `20`/`2O`, `1`/`3`/`4`/`5`/`10` TON/TONNER, LOW BED, RIGID) and job count
- [x] 1.3 Add step 3 (apply) and step 4 (review):
  - step 3: set `OrderableTruck = 1` on exactly the previewed trucks (never set 0); set `SizeClass` from the same CASE only where it is NULL
  - step 4: list orderable trucks whose `SizeClass` is still NULL
- [x] 1.4 Rewrite `db/sql/TRUCK_ORDER_INDEXES.sql`: filter `UX_TruckOrderMaster_Truck_Day_Active` and its duplicate pre-check to `Active = 1 AND BookingType = 'OWN'`; extend the range index INCLUDE with `BookingType`

## 2. Backend model

- [x] 2.1 Add enum `TruckBookingType` (`OWN`, `SHARED`, `OUTSIDE`) and enum `TruckSizeClass` (`1TON`, `3TON`, `5TON`, `10TON`, `20FT`, `40FT`, `SPECIAL`, each with a display label) in `module/fleet/entity`; include a lenient `fromCode`
- [x] 2.2 `TruckOrder`: map `bookingType`, `outsideTruckName`, `outsideSupplierName`, `truckSizeClass`; make `truckRefId` nullable
- [x] 2.3 `TruckMaster`: map `sizeClass`
- [x] 2.4 Update the DTOs:
  - `TruckOrderSaveRequest`: add `bookingType` (default OWN), `outsideTruckName`, `outsideSupplierName`, `truckSizeClass`; drop `@NotNull` on `truckRefId`
  - `TruckOrderDto`: add the four new fields
  - `OrderableTruckDto`: add `sizeClass`
- [x] 2.5 Add `TruckAvailabilityDto` (counts, `freeTrucks`, `takenTrucks` with their orders, `outsideOrders`) and `TruckDayCapacityDto` (`date`, `totalTrucks`, `takenTrucks`, `freeTrucks`, `outsideOrders`); add `days` to `TruckOrderCalendarResponse`

## 3. Backend rules and API

- [x] 3.1 `TruckMasterRepository.findOrderableTrucks`: add `malevaTruck = 1`. The size filter is applied in the service instead of a second query, since the fleet is a couple of dozen rows.
- [x] 3.2 `TruckOrderRepository`:
  - `countClashes` counts only `bookingType in (OWN, SHARED)`
  - add a range query returning live orders for a company, used for day counters and availability (`findLiveInRange`)
- [x] 3.3 `TruckOrderSpecification`: the truck filter matches only non-null trucks; OUTSIDE rows are returned when no truck filter is set
- [x] 3.4 `TruckOrderServiceImpl.save`: branch on booking type per design D5:
  - OWN: truck free, else the "No truck available: {truck} is already booked on {date}." message
  - SHARED: truck already taken, else the "free — book it as own" message
  - OUTSIDE: outside truck name required; truck and clash cleared
  - OWN/SHARED clear the outside fields
  - use `saveAndFlush`; keep the order number unchanged on edit; no catch blocks
- [x] 3.5 `TruckOrderServiceImpl.availability(companyRefId, orderDate, sizeClass, excludeId)` implementing the D4 counting rule; `findClash` narrowed to OWN/SHARED
- [x] 3.6 `TruckOrderServiceImpl.search`: fill `days` for every date when both dates are present and the range is ≤ 62 days, reusing the same counting rule; `toDto` and `truckNames` handle a null truck
- [x] 3.7 `TruckOrderController`: add `GET /api/truck-orders/availability` (camelCase params: `companyRefId`, `orderDate` ISO date, optional `sizeClass`, `excludeId`)
- [x] 3.8 Add `TruckOrderExceptionHandler` (`@RestControllerAdvice(assignableTypes = TruckOrderController.class)`), mapping a `DataIntegrityViolationException` that names `UX_TruckOrderMaster_Truck_Day_Active` to HTTP 409 with the OWN message

## 4. Backend tests

- [x] 4.1 Unit tests for `TruckOrderServiceImpl` with mocked repositories (no DB):
  - OWN on a free truck saves
  - OWN on a taken truck is rejected with the truck and date in the message
  - SHARED on a taken truck saves
  - SHARED on a free truck is rejected
  - OUTSIDE without a name is rejected
  - OUTSIDE sent with a truck id stores no truck
  - edit OWN → OUTSIDE keeps the order number
- [x] 4.2 Unit tests for availability counting:
  - OUTSIDE is not counted
  - `excludeId` frees the edited order's truck
  - the size filter narrows; NULL size appears only under any size
  - `Active = 2` and `MalevaTruck = 0` are enforced in JPQL, so they are covered by the IT
- [x] 4.3 Unit test for the calendar `days` (one entry per date; not filled for a range over 62 days) and for the exception advice's 409 mapping
- [x] 4.4 Update `TruckOrderServiceImplIT` expectations: availability route mapped, the new already-booked message, shared/outside/own-truck cases. Compile-checked off-target, not run: it needs the DB schema step and a Maven run.

## 5. Frontend data layer

- [x] 5.1 `types/truckOrder.ts`:
  - add `TRUCK_BOOKING_TYPES` and `TRUCK_SIZE_CLASSES` (code + label, in step with the Java enums)
  - extend `TruckOrder`, `OrderableTruck`, `TruckOrderSaveRequest` and `TruckOrderCalendarResponse` (`days`)
  - add `TruckAvailability`
- [x] 5.2 `api/endpoints.ts`: add `TRUCK_ORDER.AVAILABILITY`; `truckOrderApi.availability(...)`; normalise `days` to `[]` in `search`
- [x] 5.3 `queryKeys.ts` + `truckOrderQueries.ts`: add `useTruckAvailability(companyRefId, orderDate, sizeClass, excludeId, enabled)` with `staleTime: 0`; the save/delete mutations also invalidate availability keys

## 6. Frontend order dialog

- [x] 6.1 `TruckOrderDialog`: add a Size select (default "Any size") after the date, and replace the full truck select with the free-truck list from availability
- [x] 6.2 Add the "No truck available on {date} ({size})" panel with Share, Use outside truck and Cancel when `freeCount = 0`; keep an "Other booking" link to Share/Outside when trucks are free
- [x] 6.3 Share view: list the taken trucks with their existing orders (order no, type, status, remarks); saving sends `bookingType: 'SHARED'` and the chosen truck
- [x] 6.4 Outside view: required outside truck name (max 100) and optional supplier (max 200); saving sends `bookingType: 'OUTSIDE'` with no truck
- [x] 6.5 Edit mode: open on the order's booking type, pass `excludeId`, keep the read-only order number, status, remarks and Delete as today
- [x] 6.6 On a 409 save conflict, refetch availability and show the refreshed state; keep the error toast

## 7. Frontend calendar views and export

- [x] 7.1 `model/calendar.ts`: helpers to look up a day's capacity from `days`, count orders per truck-day, and select outside orders per date (pure functions)
- [x] 7.2 `MonthCalendar`: show `Free n/N` per day; highlight full days (`freeTrucks = 0`, `totalTrucks > 0`)
- [x] 7.3 `WeekGrid` and `TruckMonthGrid`: `Free n/N` in day headers, a `+n` badge for extra orders on a truck-day, and an Outside row listing outside orders per date
- [x] 7.4 `DayDetailPanel`: add a Type column and the outside truck name for OUTSIDE rows; take the free/taken counts and free chips from the day's capacity (server availability for the selected day)
- [x] 7.5 `TruckOrderCalendarPage` Excel export: add the Type, Outside Truck and Supplier columns

## 8. Frontend checks

- [x] 8.1 Extend `model/calendar.test.ts` for the new helpers (capacity lookup, per-truck-day count, outside orders by date)
- [x] 8.2 Run `npm run typecheck` and `npm run test -- --run` for the truck-order feature; fix failures

## 9. Rollout (user-run, in this order)

- [ ] 9.1 User runs step 1 of `TRUCK_ORDER_FLEET_FROM_PLANNING.sql` on the target DB
- [ ] 9.2 User runs step 2, reviews the truck list and size classes, runs step 3, and fixes the step 4 NULL size classes by hand
- [ ] 9.3 Deploy the backend; confirm the startup log shows no JPA mapping errors for `TruckOrderMaster`/`TruckMaster`
- [ ] 9.4 Deploy the frontend. Verify on `/TruckMaster/TruckOrderTimetable`:
  - free list for a day
  - no-truck panel when all trucks are taken
  - Share and Outside saves
  - calendar counters and export
- [ ] 9.5 User runs the rewritten `TRUCK_ORDER_INDEXES.sql`
- [ ] 9.6 Resolve the design's open questions (legacy .NET page still in use, 180-day window, 4 TON class, supplier source) and update design.md if any answer changes the plan
