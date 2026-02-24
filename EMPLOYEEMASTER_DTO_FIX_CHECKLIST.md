# EmployeeMasterDto - "Cannot Find Symbol" Error - Fix Checklist

## Problem
You're getting this error:
```
java: cannot find symbol
  symbol:   class EmployeeMasterDto
  location: class my.maleva.api.controller.EmployeeMasterController
```

## Root Cause
The Java compiler cannot recognize the EmployeeMasterDto class, even though the file exists.

## Step-by-Step Fix

### ✅ Step 1: Verify File Exists
- Location: `src/main/java/my/maleva/api/dto/EmployeeMasterDto.java`
- Status: **FILE EXISTS AND IS CORRECT** ✓

### ✅ Step 2: Verify Import Statement
In `EmployeeMasterController.java`, verify this import exists at the top:
```java
import my.maleva.api.dto.EmployeeMasterDto;
```
- Status: **IMPORT IS PRESENT** ✓

### ✅ Step 3: Clear IDE Cache (MOST COMMON SOLUTION)
**In JetBrains IntelliJ IDEA:**
1. Click **File** menu
2. Select **Invalidate Caches**
3. Click **Invalidate and Restart**
4. Wait for IDE to restart and reindex all files

**OR use keyboard shortcut:**
- Press `Ctrl + Shift + A` (Windows)
- Type "Invalidate Caches"
- Press Enter

### ✅ Step 4: Rebuild the Project
**Option A - Using IDE:**
1. Click **Build** menu
2. Click **Rebuild Project**
3. Wait for build to complete (check status bar at bottom)

**Option B - Using Terminal:**
```bash
cd C:\karthickworkspace\malevanew\malevabackend\Maleva
mvn clean install
```

### ✅ Step 5: Clean and Build Main
If still not working:
1. **Build** → **Clean Project**
2. **Build** → **Build Project**

## File Details

### EmployeeMasterDto.java
- **Package:** `my.maleva.api.dto`
- **Path:** `src/main/java/my/maleva/api/dto/EmployeeMasterDto.java`
- **Status:** ✓ Complete and error-free
- **Main Fields:**
  - id, employeeName, employeeType
  - company/account references
  - contact information
  - role management
  - tax and banking details

### EmployeeMasterController.java
- **Package:** `my.maleva.api.controller`
- **Path:** `src/main/java/my/maleva/api/controller/EmployeeMasterController.java`
- **Status:** ✓ Has correct import statement
- **Methods:** create, update, get, list, delete

### EmployeeMasterService.java
- **Package:** `my.maleva.api.service`
- **Path:** `src/main/java/my/maleva/api/service/EmployeeMasterService.java`
- **Status:** ✓ Has correct import statement

## Why This Error Occurs

| Cause | Solution |
|-------|----------|
| IDE cache out of sync | File → Invalidate Caches → Restart |
| Classes not compiled | mvn clean install |
| Missing import | Add: `import my.maleva.api.dto.EmployeeMasterDto;` |
| Build artifacts deleted | Rebuild project |

## ✨ Most Likely Solution
**99% of the time**, this error is fixed by:
1. **File** → **Invalidate Caches and Restart** (in JetBrains IDE)
2. OR running `mvn clean install` in terminal

Try these TWO THINGS FIRST before anything else.

## Verification
After fixing, you should see:
- ✓ No red squiggly lines under EmployeeMasterDto
- ✓ No errors in Problems tab
- ✓ Project builds successfully
- ✓ No compilation errors

