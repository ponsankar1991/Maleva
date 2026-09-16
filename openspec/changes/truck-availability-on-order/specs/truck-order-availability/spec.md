## ADDED Requirements

### Requirement: Orderable fleet and size class
The system SHALL treat a truck as bookable on the Truck Order Calendar only when it is active, marked orderable, and owned by the company (`Active = 1`, `OrderableTruck = 1`, `MalevaTruck = 1`). Each truck SHALL carry an optional size class from the fixed list `1TON`, `3TON`, `5TON`, `10TON`, `20FT`, `40FT`, `SPECIAL`. The size class SHALL be stored separately from the free-text `TruckType`, which SHALL NOT be changed.

#### Scenario: Subcontractor truck is not bookable
- **WHEN** a truck is active and orderable but has `MalevaTruck = 0`
- **THEN** it does not appear in the orderable truck list, in availability results, or in any day's truck count

#### Scenario: Truck without a size class
- **WHEN** an orderable own truck has no size class
- **THEN** it appears when availability is requested for any size, and does not appear when a specific size class is requested

#### Scenario: TruckType is untouched
- **WHEN** a truck's size class is set
- **THEN** its `TruckType` value is unchanged

### Requirement: Free trucks for a date and size
The system SHALL provide, for a company, a date and an optional size class, the number of orderable own trucks, how many are taken, how many are free, the free trucks, the taken trucks with their live orders, and the day's outside orders. A truck SHALL count as taken when it has at least one live (`Active = 1`) order of booking type `OWN` or `SHARED` on that date. `OUTSIDE` orders SHALL NOT count against any truck.

#### Scenario: Some trucks are free
- **WHEN** 20 orderable own trucks exist and 6 of them have a live OWN or SHARED order on 20 Sep
- **THEN** availability for 20 Sep reports 20 trucks, 6 taken, 14 free, and lists the 14 free trucks

#### Scenario: Size filter narrows the result
- **WHEN** availability for 20 Sep is requested with size class `40FT`
- **THEN** only trucks with size class `40FT` are counted and listed

#### Scenario: Outside orders are not counted
- **WHEN** 3 OUTSIDE orders exist on 20 Sep
- **THEN** the taken and free counts are unchanged, and the 3 orders are listed as outside orders

#### Scenario: Editing an order does not block its own truck
- **WHEN** availability is requested with `excludeId` set to an order that is the only booking on its truck that day
- **THEN** that truck is reported as free

#### Scenario: Deleted orders are ignored
- **WHEN** a truck's only order on 20 Sep has `Active = 2`
- **THEN** the truck is reported as free on 20 Sep

### Requirement: Every truck for the day, with its load
The order dialog SHALL list every orderable own truck for the chosen date in one dropdown, free trucks first, each showing its size class and how many orders it already holds that day. Selecting a truck that holds no order SHALL book as `OWN`; selecting a truck that already holds orders SHALL book as `SHARED` and SHALL first show that truck's orders for the day. An outside truck SHALL always be reachable from the dialog.

#### Scenario: The dropdown shows the load
- **WHEN** the user opens Add order for 20 Sep and BPR 7151 (40 FT) already has 2 orders that day while JQX 7151 (40 FT) has none
- **THEN** both trucks are listed, JQX 7151 shown as free and BPR 7151 shown with 2 orders, each with its size

#### Scenario: Picking a loaded truck shares it
- **WHEN** the user selects BPR 7151, which already has 2 orders on the day
- **THEN** those 2 orders are shown, the dialog states that saving adds this order to the same truck, and the save sends booking type `SHARED`

#### Scenario: Picking an empty truck books it
- **WHEN** the user selects JQX 7151, which has no order on the day
- **THEN** the dialog states the truck is free on that date and the save sends booking type `OWN`

#### Scenario: Availability follows the inputs
- **WHEN** the user changes the order date or the size in the dialog
- **THEN** the truck list and its load counts are refreshed for the new date and size

### Requirement: No truck available warning
When no orderable own truck of the chosen size is free on the chosen date, the order dialog SHALL say so before any truck is picked, naming the date and how many trucks are booked, and SHALL leave the user the choice of sharing a booked truck, using an outside truck, or cancelling.

#### Scenario: All trucks taken
- **WHEN** the user opens Add order for 20 Sep with size `40FT` and every `40FT` own truck is taken that day
- **THEN** the dialog shows "No truck available" naming 20 Sep and the number of trucks, and still lists those trucks so one can be shared

#### Scenario: Cancel saves nothing
- **WHEN** the user cancels from that state
- **THEN** the dialog closes and no order is created

#### Scenario: No warning while a truck is free
- **WHEN** at least one own truck of the chosen size is free on the chosen date
- **THEN** no "no truck available" warning is shown

### Requirement: Own booking allows one order per truck per day
An order of booking type `OWN` SHALL require an orderable own truck. It SHALL be rejected when the truck already has another live `OWN` or `SHARED` order on the same date. The rejection message SHALL name the truck and the date. The database SHALL enforce at most one live `OWN` order per company, truck and date, so concurrent saves cannot both succeed.

#### Scenario: Truck already booked
- **WHEN** a user saves an OWN order for JQX 7151 on 20 Sep and JQX 7151 already has a live OWN order that day
- **THEN** the save is rejected with a message stating that JQX 7151 is already booked on 20 Sep, and no order is created

#### Scenario: Concurrent own bookings
- **WHEN** two users save an OWN order for the same free truck and date at the same time
- **THEN** exactly one order is saved, and the other user receives the already-booked message

#### Scenario: Conflict refreshes the dialog
- **WHEN** an OWN save is rejected because the truck was taken after the dialog loaded
- **THEN** the dialog refreshes availability and shows the current free trucks or the no-truck-available panel

### Requirement: Shared booking
An order of booking type `SHARED` SHALL require an orderable own truck that already has at least one other live `OWN` or `SHARED` order on the same date. Its save SHALL NOT be blocked by the one-order-per-truck-per-day rule. Before the user confirms, the Share action SHALL list each taken truck together with its existing orders for that day.

#### Scenario: Share with a booked truck
- **WHEN** the user chooses Share, picks BPR 7151 (which has one OWN order on 20 Sep), and saves
- **THEN** a SHARED order is created on BPR 7151 for 20 Sep, and the truck remains counted once as taken

#### Scenario: Share on a free truck is refused
- **WHEN** a SHARED order is saved for a truck that has no other live order on that date
- **THEN** the save is rejected with a message that the truck is free and should be booked as own

#### Scenario: Deleting the host order keeps shared orders
- **WHEN** the OWN order on a truck is deleted while a SHARED order on the same truck and date remains live
- **THEN** the SHARED order is unchanged, and the truck is still counted as taken that day

### Requirement: Outside truck booking
An order of booking type `OUTSIDE` SHALL have no own truck, SHALL require an outside truck name, and MAY record an outside supplier name. Its save SHALL NOT be subject to any truck clash rule. Outside orders SHALL still receive an order number and appear in the calendar list, day panel and Excel export.

#### Scenario: Book an outside truck
- **WHEN** the user chooses Use outside truck, enters truck name "ABC 1234" and supplier "XYZ Transport", and saves for 20 Sep
- **THEN** an OUTSIDE order numbered like other orders is created for 20 Sep with no own truck, and 20 Sep's taken and free counts are unchanged

#### Scenario: Outside truck name missing
- **WHEN** an OUTSIDE order is saved with a blank outside truck name
- **THEN** the save is rejected with a message that the outside truck name is required

#### Scenario: Own truck sent with an outside order
- **WHEN** an OUTSIDE order is saved with a truck id
- **THEN** the stored order has no truck

### Requirement: Existing orders and booking type on edit
Every order that existed before this change SHALL be treated as booking type `OWN`. Editing an order SHALL allow its booking type to change, and the rules for the new booking type SHALL be re-checked on save. The order number SHALL NOT change on edit.

#### Scenario: Existing order after migration
- **WHEN** the calendar loads an order created before this change
- **THEN** its booking type is OWN

#### Scenario: Change own to outside
- **WHEN** a user edits an OWN order, switches it to Use outside truck with a truck name, and saves
- **THEN** the order becomes OUTSIDE with no own truck and keeps its order number, and its former truck becomes free that day unless another order holds it

### Requirement: Day capacity on the calendar
For each day in the loaded range, the calendar SHALL show the number of orderable own trucks, how many are taken and how many are free, using the same counting rule as availability. A day with zero free trucks, and at least one orderable truck, SHALL be highlighted as full in the Monthly, Weekly and Per Truck views. The day panel SHALL show each order's booking type and, for outside orders, the outside truck name. The Weekly and Per Truck views SHALL show outside orders in a separate Outside row, and SHALL mark a truck-day that holds more than one order with the extra order count.

#### Scenario: Full day highlighted
- **WHEN** all orderable own trucks are taken on 20 Sep
- **THEN** the 20 Sep cell in the Monthly view shows Free 0 of N and is highlighted as full

#### Scenario: Shared count on a truck-day
- **WHEN** BPR 7151 has one OWN and two SHARED orders on 20 Sep
- **THEN** the Weekly view cell for BPR 7151 on 20 Sep shows the booking with an extra count of 2

#### Scenario: Outside orders in the week view
- **WHEN** an OUTSIDE order exists on 20 Sep
- **THEN** the Weekly view shows it in the Outside row under 20 Sep, not in any own truck row

#### Scenario: Export includes booking details
- **WHEN** the user exports the loaded orders to Excel
- **THEN** each row includes the booking type, outside truck name and supplier columns in addition to the existing columns
