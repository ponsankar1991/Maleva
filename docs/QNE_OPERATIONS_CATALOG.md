# QNE Operations Catalog — Java (Spring Boot) Migration Reference

Synthesized from all QNE call sites in the legacy .NET (MalevaWeb/Maleva) codebase. Grouped by QNE endpoint. "Dead" = code exists but can never execute; "Commented" = code disabled by comments. Base URL: `https://api.qne.cloud/api/` (`qneapilist.qneurl`).

---

## 1. Endpoint Catalog

### 1.1 `Customers`

| Operation | Method | Status |
|---|---|---|
| Create customer | POST | **Live** |
| Update customer | PUT | **Dead** (payload built, never dispatched) |
| Backfill QNE ids by CompanyCode | GET `?$filter=companyCode in [...]` | **Live** |
| Full pull sync | GET `?top=1000` | **Live** |

**Create customer** — `CustomerServices.InsertCustomer` (line 197), after `SP_Customer` returns Result==1 and only for new customers (Id==0 or empty CompanyCode). Gated by `qneapi==true`.
- Payload (`CustomerQNEInsertModel`): `CompanyName`, `CompanyName2` (both = CustomerName), `ControlAccount` = `"700-0000"`, `Currency` (SymbolMaster.SName lookup), `Address1..Address4` (Address1 split into 100-char chunks), `ContactPerson` = **local City field** (intentional cross-mapping), `Email` = OEmail, `PhoneNo1` = OPhone, `Status` = "ACTIVE".
- Response read back: `Id` (GUID), `CompanyCode` (`CustomerQNEModel`).
- DB side effects: `UPDATE Customer SET UpdateId=<QNE Id>, CompanyCode=<QNE CompanyCode>`. **Note:** `Customer.UpdateId` stores the QNE GUID (misleading column name; other entities use `QNEId`).
- Failure: local row already committed; returns `IsSuccess=false` but `StatusCode=Success`.

**Update customer (DEAD)** — same method, existing-customer branch (line 153). `CustomerQNEUpdateModel` fully built (`Id`=UpdateId, `CompanyCode`, ContactPerson assigned City then overwritten by OName) with Type=3, but dispatch is `if (Type == 2)` only. **Customer edits never reach QNE.**

**Backfill** — `UpdateCustomerId1` (line 386), **not gated** by `qneapi`. GET with QNE-specific bracket syntax `?$filter=companyCode in ['C1','C2',...]` (only `&`→`%26` escaped). For each result: `UPDATE Customer SET UpdateId=<Id> WHERE CompanyCode=<CompanyCode>`.

**Pull sync** — `UpdateCustomerId` (line 458), **not gated**. GET `?top=1000` (no `$` prefix). Matched CompanyCodes → backfill UpdateId. Unmatched → insert local Customer via `SP_Customer` (CustomerName=CompanyName; Address1 = Address1..4 joined with `\n`; **City = ContactPerson**; phones=PhoneNo1; PaymentTermsRefid by TermsName==Term; SymbolRefid by SName==Currency), then set CompanyCode/UpdateId.

---

### 1.2 `Suppliers`

Mirror image of Customers. Local columns are `Supplier.QNEId` / `Supplier.QNECode`.

**Create supplier** — `SupplierServices.InsertSupplier` (line 185), after `SP_Supplier` Result==1, gated by `qneapi`.
- Payload (`SupplierQNEInsertModel`): `CompanyName`/`CompanyName2`, `ControlAccount` = `"800-2000"`, `Currency`, `Address1..4` (100-char split), `IsProspect=false`, `IsSuspended=false`, `IsExceedCreditAllowed=false`, `IsTaxExempted=false`, `ContactPerson` = City, `Email`, `PhoneNo1`.
- Response: `Id`, `CompanyCode` (`SupplierQNEModel`) → `UPDATE Supplier SET QNEId, QNECode`.
- Update branch (Type=3, `SupplierQNEUpdateModel`) is **DEAD** — same `if (Type==2)` guard. Supplier edits never reach QNE.

**Backfill** — `UpdateSupplierId1` (line 481), not gated. GET `?$filter=companyCode in [...]`. Deserializes into `List<CustomerQNEModel>` (**customer model reused for suppliers**). Writes `Supplier.QNEId`.

**Pull sync** — `UpdateSupplierId` (line 553), not gated. GET `?top=1000`. Unmatched QNE suppliers inserted locally via `SP_Supplier` with `SupplierType='VENDOR'` hardcoded; SP failure branch silently empty.

---

### 1.3 `Stocks`

**Create stock** — `ItemMasterServices.InsertItemMaster` (line 127), after `SP_ItemMaster` Result==1, gated by `qneapi`.
- Payload (`ProductInsetQNE`): `StockCode`=ProductCode.Trim(), `StockName`=ProductName, `BaseUom`=UOM, `IsBundled=true`, `StockControl=true`, `UseSerialNo=true`, `UseBatchNo=true` (**all four hardcoded true**), `ListPrice`=SalesRate, `MinPrice`=SalesRate, `PurchasePrice`=PurchaseRate.
- Response (`ProductInsetQNEGET`): `Id`, `StockCode` → `UPDATE ItemMaster SET QNEId, QNECode`.
- Update branch is **DEAD** (empty `ProductInsetQNE`, Type=3, never sent). Beware: `UrlData` is first set to `customerapi` (copy-paste artifact, line 101) before being overwritten with `Stocksapi` on the live path.

**Reconcile + push** — `UpdateItemmasterId` (line 467/505), not gated.
- GET `?$filter=stockCode in [...]` → for matches: `UPDATE ItemMaster SET QNECode, QNEId` (matched on `trim(Prod_Code)`).
- For local items absent in QNE: per-item POST with same `ProductInsetQNE` shape; on individual failure, **log and continue** (fail-fast abort deliberately commented out at 517-520).

---

### 1.4 `SalesInvoices`

**Create invoice** — TWO nearly identical live call sites, both POST `SaleInvoiceMasterQneInsertModel`, both gated by `qneapi`, both fire only when `SaleMaster.QNECode` is empty:
1. `SaleInvoiceServices.InvoiceConvert` (line 284)
2. `SaleOrderServices.InvoiceConvert` (line 1875) — SO→invoice conversion path after `SP_InvoiceMaster`

- Header payload: `Customer`=Customer.CompanyCode, `InvoiceDate`=SaleMaster.SaleDate, `Term`=TermsName, `Attention`=Customer.City, `ReferenceNo`=Remarks1, `Ref1`=Origin, `Ref2`=Destination, `Ref3`=Quantity, `Ref4`=TotalWeight, `Ref5`=Offvesselname, `Remark1`=Loadingvesselname, `Remark2`=DoMaster.CNumberDisplay, `Remark3`=Commodity, `Title`='SALES' if both vessel names empty else Loadingvesselname, `CurrencyRate`=CurrencyValue, `IsTaxInclusive`, `IsRounding`.
- **Multi-reference rule:** when the invoice covers >1 `SaleMasterReference` rows, Ref1–Ref5/Remark1–3/Title are all sent as empty strings.
- Details (`SaleInvoiceDetailQneInsertModel` per SaleDetails row): `Stock`=HtmlEncode(Prod_Code), `Description`=SDRemarks else PName, `Qty`, `Uom`=UOM.Description, `UnitPrice`=SalesRate, `DateRef1`=`DateRef2`=InvoiceDate, `TransferFrom`={} (empty object).
- Response (`SaleInvoiceMasterQneReturnModel`): `Id`, `InvoiceCode` → `UPDATE SaleMaster SET QNEId, QNECode`.
- ⚠️ **Contradiction between the two sites:** SaleInvoiceServices sends `IsTaxInclusive=false, IsRounding=false`; SaleOrderServices sends `true, true` (see §4).

**Update invoice** — `SaleInvoiceServices.InvoiceConvertEdit` (line 551), **live PUT** (Type=3), fires only when `QNECode` is non-empty. Payload `SaleInvoiceMasterEditQneInsertModel`: same real fields plus ~30 hardcoded filler fields (`isApproved=true`, `isTaxInclusiveOnly=true`, `isRounding=true`, all amounts 0, all strings empty); detail filler likewise (`isPartialTransfer=true`, `isBundled=true`, `isSubItem=true`, zeros). The QNEId/QNECode write-back after the PUT is **dead** (guarded by `Type==2`). This is the **only live PUT in the entire codebase**.

**Commented:** `/Find?code={QNECode}` GET + fetch-then-merge `SaleInvoiceMasterQneUpdateModel` PUT flow exists in comments in both services (abandoned design). In `SaleOrderServices` the whole already-synced else-branch is commented, so re-converting a synced invoice there is a silent no-op.

**Pending-push queue:** `SelectSaleInvoice` with request flag `checkqnepush=true` filters `isnull(QNECode,'')=''` ordered by BillDate asc — a UI listing of never-pushed invoices. No API call.

---

### 1.5 `SalesCNs` (credit notes)

**Create CN** — `SaleCreditServices.SaleCreditVIEW` (line 277), gated by `qneapi`, fires **on view/print (F5)**, not on save, when `SaleCreditMaster.QNECode` is empty.
- Payload (`SaleCreditMasterInsertQNE`): `Customer`=CompanyCode, `CnDate`=SaleDate, `Term`, `ReferenceNo`=**original invoice's SaleMaster.QNECode**, `Attention`=City, `CurrencyRate`, `IsTaxInclusive=true`, `IsRounding=true` (hardcoded). Details (`SaleCreditDetailInsertQNE`): `Stock`=HtmlEncode(Prod_Code), `Description`=PName, `Qty`, `Uom`, `UnitPrice`, `DateRef1/2`.
- Response (`SaleCreditMasterQneGetModel`): `Id` (Guid), `CnCode` → `UPDATE SaleCreditMaster SET QNEId, QNECode`.
- Failure aborts the whole view/print call.

**`SalesCNs/Knockoff`** — **entirely commented out** (line 285). Intended payload: `ReceiptKnockOffInsertQne` { `DocId` = CN insert response Id, `KnockoffItems`: [{ `Payment` = SaleCreditKnockOff.SaleCreditAmount, `KnockoffRefId` = invoice's SaleMaster.QNEId }] }. CN-to-invoice knockoff never reaches QNE today.

**Update CN** — **dead** (empty model, Type=3, `Type==2` guard, line 337).

---

### 1.6 `CustomerReceipts` (+ `/Knockoff`, `/Match`)

All in `ReceiptServices.ReceiptVIEW` (lines 723–863) — again sync happens **on view**, gated by `qneapi`, only when `Receipt.QNECode` is empty.

**Create receipt** — POST (line 792). Payload (`ReceiptInsertQne`): `CustomerCode`=Customer.CompanyCode, `DocDate`=ReceiptDate, `Amount`, `BankCharges`, `DepositAccountCode`=**BankMaster.QneCode** (bank account must be pre-mapped), `CurrencyRate`.
- Response (`ReceiptReturnQne`): `Id`, `DocCode` → `UPDATE Receipt SET QNEId, QNECode`; `Id` also becomes the knockoff `DocId`.

**Knockoff** — POST `CustomerReceipts/Knockoff` (line 800), immediately after successful insert. Payload (`ReceiptKnockOffInsertQne`): `DocId`=receipt QNE Id (assigned by reference after `Data` was already set — order-sensitive), `KnockoffItems`: [{ `Payment`=ReceiptDetails.ReceiptAmount, `KnockoffRefId`=**SaleMaster.QNEId** of the invoice }] per ReceiptDetails row.
- **BUG:** response `ro2` is stored but code re-checks `ro1` — knockoff failures are silently ignored, nothing persisted.

**`CustomerReceiptMatchs` + receipt update** — **entirely commented out** (line 765): intended re-sync (`ReceiptUpdateQne` with Id/DocCode) + Match for already-synced receipts. Already-synced receipts send nothing.

---

### 1.7 `Bills`

**Create bill** — `BillMasterServices.BillMasterConvert` (line 204), gated by `qneapi`, when `BillMaster.QNECode` empty.
- Payload (`BillsQneMasterInsertModel`): `BillCode`=cnumberdisplay, `BillDate`=saledate, `BillFrom`=SupplierName+','+Address1, `Supplier`=**Supplier.QNECode**, `ReferenceNo`=Remarks, `Term`, `Currency`=SymbolMaster.SName, `CurrencyRate`, `Description`, `SupplierInvNo`=InvoiceNo, `IsTaxInclusive=false`, `IsRounding=false`, `DueDate`=`PostDate`=BillDate. Details (`BillsQneDetailInsertModel` — **details ARE sent**): `Account`=GLAccounts.GLAccountCode, `Description`=RemarksD, `Amount`.
- Response (`BillsQneMasterReturnModel`): `Id`, `BillCode` → `UPDATE BillMaster SET QNEId, QNECode`.
- Commented: `qneurl + Billsapi + "/Find?code="` GET + `BillsQneMasterUpdateModel` PUT (dead update path).

### 1.8 `PayBills`

**Create payment** — `PaymentServices.PaymentConvert` (line 222), gated by `qneapi`, when `Payment.QNECode` empty.
- Payload (`PaymentQNEInsertModel`) — **master only, NO detail lines** (detail loop commented out): `PaymentDate`, `PayByAccount`=**BankMaster.QNECode**, `Supplier`=Supplier.QNECode, `PayTo`=SupplierName, `ReferenceNo`=RefNumber, `CurrencyRate`, `BankChargesAmount`, `TaxDate`/`ChequeDate`/`BouncedChequeDate`/`ChequePreparedDate` all = PaymentDate, `TotalAmount`=Amount, `IsBouncedCheque`/`IsCancelled`/`IsPostDatedCheque`/`IsTaxInclusive`/`IsRounding` = false. Currency selected in SQL but **never mapped**.
- Response (`PaymentVoucherQNEReturnModel`): `Id`, `PaymentCode` → `UPDATE Payment SET QNEId, QNECode`.
- Commented update path uses **`PaymentVouchersapi/Find?code=`** — a different endpoint than the live insert (PayBills vs PaymentVouchers) for the same document.

### 1.9 `PaymentVouchers`

**Create voucher** — `PaymentVoucherServices.PaymentVoucherConvert` (line 249), gated by `qneapi`, when `PaymentVoucherMaster.QNECode` empty.
- Payload (`PaymentVoucherQNEInsertMasterModel`): `PaymentDate`=PaymentVoucherDate, `PayByAccount`=BankMaster.QNECode, `PayTo`, `ReferenceNo`=RefNo, `Currency`=**hardcoded 'RM'**, `CurrencyRate`, `Description`, `BankChargesAmount`, all four dates = PaymentVoucherDate, all boolean flags false. Details (`PaymentVoucherQNEInsertDetailModel` — sent, unlike PayBills): `Account`=GLAccountCode (joined on `RowIndex = AccountGroupRefId`), `Description`, `Amount`.
- Response: `Id`, `PaymentCode` → `UPDATE PaymentVoucherMaster SET QNEId, QNECode`.
- `/Find?code=` + Type=3 update: commented (lines 192–245); re-converting a synced voucher is a silent no-op.

### 1.10 Report URL endpoints (`Reports/...`)

All GET, response JSON `QNEURLModel { file }` = QNE-hosted document URL returned to the UI as `Data1`. **Double-slash bug:** `Report = qneurl + "/Reports"` yields `.../api//Reports/...` in every one.

| Endpoint template | Caller | Gate | Id source |
|---|---|---|---|
| `Reports/SalesInvoices/{Id}/Url` | SaleInvoiceServices.InvoiceConvert (450) & InvoiceConvertEdit (778); SaleOrderServices.InvoiceConvert (1907) | `qneview` | `SaleMaster.QNEId` |
| `Reports/Receipts/{Id}/Url` | ReceiptServices.ReceiptVIEW (838) | `qneview` | `Receipt.QNEId` |
| `Reports/SalesCN/{Id}/Url` | SaleCreditServices.SaleCreditVIEW (374) | `qneview` | `SaleCreditMaster.QNEId` |
| `Reports/CustomerStatement/Url?customerId={QNEID}&year={Y}&month={M}` | CustomerReportServices.SelectCustomerStatement (110) | **`qnereportview`** (currently TRUE) | `Customer.UpdateId` |

When the gate is true, the method returns the QNE URL and **skips the local report path entirely**. Controller contract (11 sites across SaleInvoice/SaleCredit/Receipt/PaymentVoucher/SaleOrder controllers + SaleInvoiceAppController): `qneview==false` → `Session["reportdata"]=Data1`, JSON `{ok:true, Data1:"", Message}`; `qneview==true` → JSON `{ok:true, Data1:<QNE URL>, Message}`. Note flags: `qneview=false`, `qnereportview=true` in shipped config — so only CustomerStatement actually uses a QNE-hosted report today.

### 1.11 Direct SQL into the QNE database (non-HTTP)

`Dapperr.GetDbconnection1` (Services/Dapperr.cs line 91): a **second SqlConnection** to QNE's own SQL Server DB, connection string `QneCon` / `QneConDemo` selected by `qneapilist.qnedemo`. All `*1`-suffixed helpers (`Execute1`, `ExecuteScalar1`, `Get1<T>`, `GetAll1<T>`) run caller-supplied raw SQL directly against QNE's database, bypassing the REST API. Connection cached unclosed per instance, not thread-safe, `Dispose()` throws. **The Spring Boot port must decide: preserve the dual-datasource, or replace direct QNE-DB SQL with API calls.** (Call sites of the `*1` methods were not in the extraction scope — see §4.)

---

## 2. Cross-cutting transport semantics

Every HTTP operation goes through the single helper `commonfunctions.QneApi(QneSendModel)` (commonfunctions.cs line 781):

- **`QneSendModel { UrlData, Data, Type }`** — Type 1=GET, 2=POST, 3=PUT. `UrlData` is the **full URL** including query string (qneapilist constants already contain the base; QneApi does not prepend anything, despite commented code suggesting otherwise in places).
- **Headers:** `DbCode: OUCMLM` (live) or `OUCMLM_TRIAL_V1` (`qnedemo=true`); `Accept: application/json`. **No authentication header of any kind** — verify how the tenant is actually authenticated before porting (DbCode appears to be the only credential).
- Body: `JsonConvert.SerializeObject(Data)` as UTF-8 `application/json` (POST/PUT only).
- TLS 1.2 forced process-wide; **30-minute timeout**; all calls synchronous `.Result` blocking. No retries, no request logging.
- **Response contract (`ResponseViewModel {IsSuccess, Message}`):** 2xx → `IsSuccess=true`, `Message`=raw body. 404/400 → `IsSuccess=false`, `Message`=raw body (callers parse). Any other non-success → body parsed as `ErrorMsg{code,Message}`, `Message = code + "\n" + Message`. Exceptions → unwrapped to innermost, `Message=ToString()`.
- **Universal caller quirks to consciously preserve or fix:**
  - On QNE failure after a successful local SP, every insert path returns `IsSuccess=false` **with `StatusCode=Success`** (mixed signal to UI).
  - The local row is committed *before* the QNE call — QNE failure leaves a row with empty QNEId/QNECode; the ungated reconcile methods (UpdateCustomerId1/UpdateSupplierId1/UpdateItemmasterId) exist to backfill.
  - Exceptions inside QNE blocks are swallowed (logged only); explicit `IsSuccess=false` returns usually abort the whole operation (including blocking print flows).
  - Update-to-QNE is dead nearly everywhere: payloads built with Type=3 but dispatch guarded by `if (Type == 2)`. Only SalesInvoices PUT (InvoiceConvertEdit) is live.
  - All SQL (source selects and QNEId/QNECode write-backs) is string-concatenated — replace with parameterized queries in Java.
- **OData conventions (QNE-flavored, non-standard):** `$filter=<field> in ['a','b']` bracket syntax; `contains()/startswith()/endswith()`; `?top=1000` used **without** `$`; only `&` is URL-encoded (`%26`).
- **Flags (static in qneapilist.cs):** `qneapi=true` gates pushes (insert methods only — reconcile methods are ungated); `qnedemo=false` selects DbCode + QNE DB connection string; `qneview=false` gates QNE-hosted document URLs for transactions; `qnereportview=true` gates the CustomerStatement report. In Spring Boot these should become externalized configuration, not compiled constants.
- **Local key columns:** convention is `QNEId` (GUID) + `QNECode` (human code) on SaleMaster, SaleCreditMaster, Receipt, Payment, BillMaster, PaymentVoucherMaster, Supplier, ItemMaster — **except Customer, which uses `UpdateId` + `CompanyCode`**.

---

## 3. Ordering dependencies

1. **Customer → SalesInvoice / SalesCN / CustomerReceipt**: all three send `Customer.CompanyCode`; the customer must exist in QNE (via InsertCustomer POST or reconcile) first.
2. **Supplier → Bills / PayBills**: both send `Supplier.QNECode`.
3. **Stock (ItemMaster) → SalesInvoice / SalesCN details**: detail lines reference `Prod_Code` as QNE `Stock` code; the stock must exist in QNE.
4. **SalesInvoice → CustomerReceipt Knockoff**: `KnockoffRefId = SaleMaster.QNEId` — the invoice must already be synced or the knockoff references an empty id.
5. **SalesInvoice → SalesCN**: CN's `ReferenceNo` = the invoice's `QNECode` (and the commented CN knockoff uses invoice `QNEId`).
6. **CustomerReceipts insert → Knockoff**: knockoff `DocId` comes from the insert response within the same call.
7. **BankMaster pre-mapping**: Receipts (`DepositAccountCode`), PayBills and PaymentVouchers (`PayByAccount`) send a QNE bank-account code stored on BankMaster — **no code in the extraction ever creates or syncs bank accounts to QNE**; that mapping must be seeded manually/out of band.
8. **GLAccounts pre-mapping**: Bills and PaymentVoucher details send `GLAccountCode`, which must match QNE's chart of accounts; no sync exists.
9. **PaymentTermsMaster / SymbolMaster**: `Term`/`Currency` names must match QNE's term and currency names (used both outbound and as match keys on pull sync).

---

## 4. Ambiguities and contradictions to verify in source

1. **IsTaxInclusive/IsRounding conflict on invoice create.** `SaleInvoiceServices.InvoiceConvert` (line 284) reportedly sends `false/false`; `SaleOrderServices.InvoiceConvert` (line 1875) sends `true/true` — same endpoint, same model. One extraction is wrong, or the two paths genuinely differ (which changes QNE amounts). Re-read both payload builders.
2. **BankMaster QNE code column name.** ReceiptServices (line 792) says `BankMaster.QneCode`; PaymentServices/PaymentVoucherServices say `BankMaster.QNECode`. Confirm the actual column name and casing in the SQL.
3. **Base-URL prepending.** Active calls pass bare `qneapilist.*api` values (which already contain the full URL), but several commented Find calls prepend `qneapilist.qneurl` (BillMasterServices line 167, ReceiptServices line 765, SaleOrderServices line 1822) — which would double the base. Confirm QneApi (commonfunctions.cs 781) never prepends anything before assuming full-URL semantics in Java.
4. **PUT target URL for InvoiceConvertEdit.** The live PUT goes to bare `SalesInvoices` with `Id` only in the body (`CI.Id = QNEId`). Verify whether QNE's PUT expects `/SalesInvoices/{id}` or the body id — SaleInvoiceServices.InvoiceConvertEdit, line 551.
5. **PayBills vs PaymentVouchers duality.** PaymentConvert inserts to `PayBills` but its commented update uses `PaymentVouchersapi/Find` and both deserialize `PaymentVoucherQNEReturnModel`. Confirm which QNE document type a Payment really is before choosing the Java endpoint — PaymentServices.PaymentConvert, lines 122/169/222.
6. **`?top=1000` without `$`.** Whether QNE honors non-`$` `top` is server-dependent; if it doesn't, the pull syncs silently sync everything or nothing predictable — CustomerServices.UpdateCustomerId (458), SupplierServices.UpdateSupplierId (553).
7. **Double-slash report URLs** (`api//Reports/...`) — works today against QNE; decide to preserve or normalize (qneapilist.cs `Report` constant).
8. **Knockoff response bug.** ReceiptServices line 800–801 checks `ro1` instead of `ro2`; decide whether Java should keep silently ignoring knockoff failures or surface them (behavioral change).
9. **Which callers use the direct QNE-DB Dapper connection.** `Dapperr` `*1` methods run arbitrary SQL against the QNE database, but the extraction scope did not include their call sites. Grep for `Execute1|ExecuteScalar1|Get1<|GetAll1<` across the solution before designing the Java datasource layer — Services/Dapperr.cs line 91.
10. **Dead-update intent.** The pervasive `if (Type == 2)` guard killing every Type=3 path (customers, suppliers, items, CNs, and QNECode persistence after invoice PUT) — determine with the business whether this is an intentional "create-once, never update" policy to preserve, or a bug to fix, before porting each one (CustomerServices 153, SupplierServices 185, ItemMasterServices 127, SaleCreditServices 337, SaleInvoiceServices 753–756).
11. **`ContactPerson` double assignment** in update models (City then OName) — irrelevant while updates are dead, but decide the correct field if updates are revived (CustomerServices 153, SupplierServices 185).
12. **Reconcile methods ungated by `qneapi`** — confirm whether the Java port should gate them (they hit QNE even when the master switch is off) — UpdateCustomerId1/UpdateCustomerId, UpdateSupplierId1/UpdateSupplierId, UpdateItemmasterId.
13. **No auth beyond `DbCode` header.** Verify against current QNE Cloud API docs whether an API key/token is now required; the legacy code sends none (commonfunctions.QneApi, line 781).
14. **`UpdateCustomerId` inverted result check** — returns error with `ro1.Message` when `result1.Count==0` even on success (CustomerServices 458); confirm intended behavior.
15. **Sync-on-view semantics.** CNs and Receipts push to QNE as a side effect of *viewing/printing* (SaleCreditVIEW, ReceiptVIEW), not saving. Confirm whether the Java design should keep this or move sync to an explicit action/queue — this changes idempotency requirements (the empty-QNECode guard is the only dedup mechanism).