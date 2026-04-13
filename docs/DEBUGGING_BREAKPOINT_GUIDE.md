# SQL Query Debugging - Breakpoint Guide

## Quick Answer: WHERE TO SET BREAKPOINTS

### **BREAKPOINT 1: Line 566 (BEST FOR SEEING COMPLETE QUERY)**
```java
String sql = getVesselPlanningBaseSql() + queryBuilder.getWhereClause() + " ORDER BY SDId DESC";
```
**Why:** At this line, the complete SQL query is constructed and ready to see

---

### **BREAKPOINT 2: Line 570-571 (BEST FOR SEEING PARAMETERS)**
```java
log.debug("SQL Query: {}", sql);
log.debug("Query Parameters: {}", Arrays.toString(queryBuilder.getParameters()));
```
**Why:** Parameters are prepared and ready to inspect

---

### **BREAKPOINT 3: Line 573-574 (BEST FOR SEEING EXECUTION)**
```java
return jdbcTemplate.query(sql, queryBuilder.getParameters(),
        new BeanPropertyRowMapper<>(VesselPlanningDashboardModel.class));
```
**Why:** Final query execution point - see both query and parameters together

---

## Step-by-Step Debug Instructions

### **STEP 1: Open DashboardRepository.java File**

1. In JetBrains IDE, press `Ctrl + Shift + O` (Open file dialog)
2. Type: `DashboardRepository.java`
3. Click on it to open
4. Or navigate to: `src/main/java/my/maleva/api/module/dashboard/repository/DashboardRepository.java`

---

### **STEP 2: Set Breakpoint at Line 566**

In the editor:
1. Click on the **line number 566** (left side of the code)
2. A red circle dot will appear ✓
3. This is your breakpoint

```
Line 566:  String sql = getVesselPlanningBaseSql() + queryBuilder.getWhereClause() + " ORDER BY SDId DESC";
           ^^^^ Click here on line number
```

---

### **STEP 3: Start Debug Mode**

1. Press **Shift + F9** OR
2. Click **Debug** button in the IDE (green triangle with bug icon)
3. The application will start in debug mode
4. You'll see: `Connected to the target VM`

---

### **STEP 4: Make API Request from Postman**

1. Open Postman
2. Create a GET request:
   ```
   GET http://localhost:8082/api/dashboard/vessel-planning/6
   ```
3. Click **Send** button
4. The debugger will STOP at line 566

---

### **STEP 5: Inspect Variables - SEE THE SQL QUERY**

When debugger stops at line 566:

**Option A - Hover Over Variables:**
```
Move mouse over "sql" variable → A popup shows the value
```

**Option B - Use Variables Panel (Better):**
1. Look at the left panel labeled "Variables" (or press **Shift + F7**)
2. Find variable: `sql`
3. Click to expand it
4. You'll see the complete SQL query!

**Example of what you'll see:**
```
sql = "SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, ...
       FROM SaleOrderMaster S WITH (NOLOCK)
       INNER JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
       WHERE S.CompanyRefId = ? AND S.Active != 2
       ORDER BY SDId DESC"
```

---

### **STEP 6: View Parameters**

1. In the Variables panel, find: `queryBuilder`
2. Click the arrow to expand it ▶
3. Expand `parameters` (ArrayList)
4. You'll see each parameter:
   - Parameter[0] = "2026-04-12" (toDate)
   - Parameter[1] = 6 (CompanyId)
   - Parameter[2] = 6 (CompanyId)

**Example:**
```
queryBuilder:
├── parameters (ArrayList)
│   ├── [0] = "2026-04-12"
│   ├── [1] = 6
│   └── [2] = 6
├── whereClause (StringBuilder) = ""
└── hasConditions (boolean) = false
```

---

### **STEP 7: Step to Next Line (See Query Execution)**

1. Press **F10** (Step Over)
2. Now you're at line 570
3. Watch the `sql` variable building the complete query

---

### **STEP 8: Step to Execution (Line 573)**

1. Press **F10** again multiple times until line 573
2. At line 573, you see:
   ```java
   return jdbcTemplate.query(sql, queryBuilder.getParameters(), ...)
   ```
3. This is where the query actually runs against the database

---

## What You Can See at Each Breakpoint

### **Breakpoint at Line 566:**
```
VISIBLE:
- sql (String) - the complete parameterized SQL query
- queryBuilder (QueryBuilder) - contains all parameters

EXAMPLE OUTPUT:
sql = "SELECT ... FROM SaleOrderMaster ... WHERE S.CompanyRefId = ? AND S.Active != 2 ORDER BY SDId DESC"
```

### **Breakpoint at Line 570:**
```
VISIBLE:
- log.debug("SQL Query: {}", sql) - SHOWS FULL SQL
- log.debug("Query Parameters: {}", Arrays.toString(...)) - SHOWS [6, 6]

CHECK CONSOLE:
DEBUG - SQL Query: SELECT DISTINCT S.Id, ...
DEBUG - Query Parameters: [2026-04-12, 6, 6]
```

### **Breakpoint at Line 573:**
```
VISIBLE:
- sql (String) - parameterized query
- queryBuilder.getParameters() - actual parameter values
- Result after execution - returned list

THIS IS THE ACTUAL EXECUTION POINT
```

---

## Console Output (Check These Also!)

While debugging, monitor the **Console Tab** for log messages:

```
DEBUG - Fetching vessel planning data for company: 6
DEBUG - Executing vessel planning query with 3 parameters
DEBUG - SQL Query: SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, S.Origin...
DEBUG - Query Parameters: [2026-04-12, 6, 6]
```

---

## Complete Debugging Workflow - Visual Map

```
┌─────────────────────────────────────────────────────┐
│ 1. START DEBUG (Shift + F9)                        │
└────────────┬────────────────────────────────────────┘
             │
┌────────────▼────────────────────────────────────────┐
│ 2. SEND API REQUEST FROM POSTMAN                   │
│    GET /api/dashboard/vessel-planning/6            │
└────────────┬────────────────────────────────────────┘
             │
┌────────────▼────────────────────────────────────────┐
│ 3. DEBUGGER STOPS AT LINE 566                      │
│    String sql = getVesselPlanningBaseSql() + ...   │
│    ⭐ HOVER OVER "sql" VARIABLE ⭐                │
└────────────┬────────────────────────────────────────┘
             │
         ┌───┴────────────────────┐
         │                        │
    YES: See SQL?             NO: See Variables panel
         │                        │
         ▼                        ▼
    Move to line 573         Click arrow to expand
    (F10 repeatedly)         queryBuilder
         │                        │
         └───────┬────────────────┘
                 │
         ┌───────▼────────────────┐
         │ 4. PRESS F10 TO STEP   │
         │    MOVE THROUGH CODE   │
         └───────┬────────────────┘
                 │
         ┌───────▼────────────────┐
         │ 5. CHECK CONSOLE TAB   │
         │    FOR LOG MESSAGES    │
         └───────┬────────────────┘
                 │
         ┌───────▼────────────────┐
         │ 6. SEE RESULTS IN      │
         │    VARIABLES PANEL     │
         └────────────────────────┘
```

---

## Keyboard Shortcuts for Debugging

| Key | Action |
|-----|--------|
| **Shift + F9** | Start Debug |
| **F9** | Continue (Resume execution) |
| **F10** | Step Over (next line) |
| **F11** | Step Into (inside method) |
| **Shift + F11** | Step Out (exit method) |
| **Ctrl + Shift + D** | Toggle Breakpoint |
| **Ctrl + Shift + L** | View breakpoints list |
| **Shift + F7** | Show Variables panel |
| **Alt + 4** | Show Console panel |

---

## ALTERNATIVE METHOD: View SQL Through Console Logs

If you don't want to use breakpoints, just:

1. Start the application: `mvn spring-boot:run`
2. Make API request from Postman
3. Look at the console output

You'll see:
```
DEBUG - Fetching vessel planning data for company: 6
DEBUG - Executing vessel planning query with 3 parameters
DEBUG - SQL Query: SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, ...
DEBUG - Query Parameters: [2026-04-12, 6, 6]
```

---

## Troubleshooting: Breakpoint Not Working

**Problem:** Debugger doesn't stop at breakpoint

**Solution:**
1. Make sure you're running in **Debug Mode** (not Run mode)
2. Recompile the code: `mvn compile`
3. Restart the debug session
4. Check that breakpoint is red (✓ enabled)
5. If still not working, try: **Ctrl + Shift + D** to toggle breakpoint off/on

---

## Multiple Breakpoints Strategy

Set breakpoints at ALL 3 locations:

1. **Line 566** - See SQL being built
2. **Line 570** - See log message in code
3. **Line 573** - See execution point

Then press **F9** (Continue) to jump between them as you debug!

---

## What The Query Looks Like (Example)

When you see the `sql` variable at line 566:

```sql
SELECT DISTINCT S.Id, S.Id as SaleOrderMasterRefId, S.Origin, S.Destination, S.CNumberDisplay as JobNo,
    S.BoatCPop, S.PermitCPop, S.ForwardingCPop, S.PortCPop, S.LiveCPop, S.MMHECPop, S.AFpoCPop,
    ... (many more columns)
FROM SaleOrderMaster S WITH (NOLOCK)
INNER JOIN Customer C WITH (NOLOCK) ON C.Id = S.CustomerRefId
INNER JOIN JobTypeMaster JT WITH (NOLOCK) ON JT.Id = S.JobMasterRefId
LEFT JOIN JobStatusMaster J WITH (NOLOCK) ON J.Id = S.JStatus
... (more joins)
WHERE S.CompanyRefId = ? AND S.Active != 2
AND S.JStatus NOT IN (6, 5, 20, 8, 15, 12, 16, 19)
AND S.JStatus NOT IN (SELECT Id FROM JobStatusMaster WHERE MId = 5 AND Active = 1 AND CompanyRefId = ?)
AND S.JStatus != ''
ORDER BY SDId DESC
```

And the parameters are: `[2026-04-12, 6, 6]`

---

## Summary

| Goal | Breakpoint Location | What You See |
|------|---------------------|--------------|
| See complete SQL | Line 566 | Full parameterized query |
| See parameters | Line 571 | Parameter array [2026-04-12, 6, 6] |
| See execution | Line 573 | Both query and params at execution |
| See console logs | Console tab | Debug messages from code |

**BEST PRACTICE:** Set breakpoint at **Line 573** - you'll see everything!


