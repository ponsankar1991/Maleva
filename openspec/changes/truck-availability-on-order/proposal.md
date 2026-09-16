## Why

Dispatchers accept customer orders from memory. The order that arrives after every own truck is already committed for that day is still accepted, and the need for an outside (hired) truck is discovered late, after the customer has been told yes. The Truck Order Calendar already books one truck per day, but it offers a hardcoded list of 14 trucks, never tells the user which trucks are still free, and simply rejects a second order on a taken truck — even though the old planning data shows trucks routinely carrying 2–8 small jobs a day.

## What Changes

- **Availability at order time.** The add/edit dialog asks for the date and (optionally) a truck size first, then lists only the own trucks still free that day. Revives the idea of the legacy `TruckServices.GetunorderedTruck`, which existed but was never called.
- **"No truck available" choice.** When no own truck of the chosen size is free, the dialog says so and offers three actions:
  - **Share** — put the order on a truck that is already booked that day. The dispatcher decides it fits; the system does no weight or pallet maths.
  - **Use outside truck** — save the order with the outside truck / supplier name. It is not counted against own trucks.
  - **Cancel** — the user tells the customer no.
- **Booking type on every order**: `OWN`, `SHARED` or `OUTSIDE`. Existing rows become `OWN`.
- **Day capacity on the calendar.** Each day shows *Trucks N / Taken / Free*, red when Free is 0, in the Monthly, Weekly and Per Truck views and the day panel.
- **Truck size class.** A fixed size list (1 TON, 3 TON, 5 TON, 10 TON, 20 FT, 40 FT, SPECIAL) recorded on the truck, separate from the free-text `TruckType` that other screens still read.
- **Fleet = trucks used in old planning.** A one-time SQL script marks as orderable the own trucks (`MalevaTruck = 1`) that planning assigned jobs to, and maps their `TruckType` text to a size class. It replaces the 14-plate legacy list as the source.
- **Clash rule narrowed.** "One order per truck per day" now applies to `OWN` bookings only; `SHARED` and `OUTSIDE` bypass it. The not-yet-applied unique index script is rewritten to match.
- **BREAKING (legacy .NET page only):** `OUTSIDE` orders have no own truck, so the legacy `/TruckMaster/TruckOrderTimetable` .NET page (INNER JOIN on TruckMaster) will not show them, and its stored procedure will refuse to re-save a `SHARED` order. The React page replaced it on the same route.

Explicitly approved change to the screen's behaviour and layout; the legacy-parity rule does not apply to these additions.

## Capabilities

### New Capabilities
- `truck-order-availability`: free/taken own trucks per day and size, the no-truck-available choice (Share / Outside / Cancel), booking types and their clash rules, day capacity counters on the calendar views, the truck size class and the orderable fleet definition.

### Modified Capabilities
<!-- None: openspec/specs holds only logback specs; the truck order calendar has no spec yet. -->

## Impact

- **Database (MalevanewDemo, scripts run by the user):**
  - `TruckOrderMaster`: new `BookingType`, `OutsideTruckName`, `OutsideSupplierName`, `TruckSizeClass`; `TruckRefId` becomes nullable.
  - `TruckMaster`: new `SizeClass`.
  - Backfill of `OrderableTruck` and `SizeClass` from planning history.
  - `db/sql/TRUCK_ORDER_INDEXES.sql`: the unique index is filtered to `BookingType = 'OWN'`.
- **Backend (`module/fleet`):**
  - `TruckOrder` and `TruckMaster` entities.
  - `TruckOrderSaveRequest`, `TruckOrderDto`, `OrderableTruckDto`, `TruckOrderCalendarResponse`.
  - `TruckOrderRepository.countClashes`, `TruckOrderSpecification`, `TruckOrderServiceImpl`.
  - `TruckOrderController`: new `GET /api/truck-orders/availability`; `/clash` narrowed to OWN.
  - New `TruckBookingType` and `TruckSizeClass` enums.
- **Frontend (`features/truck-order`):**
  - `types/truckOrder.ts`, `truckOrderApi.ts`, `truckOrderQueries.ts`, `endpoints.ts`.
  - `TruckOrderDialog` (availability step and no-truck panel).
  - `DayDetailPanel`, `MonthCalendar`, `WeekGrid`, `TruckMonthGrid` (capacity and booking type).
  - `model/calendar.ts` and its tests; Excel export columns.
- **Not affected:**
  - Sale Order, Planning and RTI screens.
  - The AI planning suggestion.
  - `TruckType` readers such as `getTruckComboByType`.
