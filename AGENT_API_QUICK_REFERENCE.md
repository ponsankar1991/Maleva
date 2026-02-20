# Quick Reference - Agent API Select All

## Problem Solved ✅

You needed to convert your .NET `SelectAgentAll` method to Spring Boot REST API.

## Solution Summary

**New Endpoint**: `POST /api/agents/select-all`

### Quick Test
```bash
curl -X POST "http://localhost:8082/api/agents/select-all?companyRefId=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Response
```json
{
  "ok": true,
  "message": "Agents retrieved successfully",
  "data": [{"id": 1, "Name": "Agent Name", ...}, {...}],
  "count": 5
}
```

## What Was Modified

| File | What's New |
|------|-----------|
| `AgentRepository.java` | 2 new @Query methods for filtering |
| `AgentService.java` | selectAgentAll() business logic method |
| `AgentController.java` | /select-all endpoint + AgentSelectionResponse class |

## Parameters

| Name | Required | Example | Purpose |
|------|----------|---------|---------|
| companyRefId | ✅ Yes | 5 | Company to filter by |
| jobId | ❌ No | 3 | Optional: filter by agent company ID |

## Filters Applied

✅ **Always**: `CompanyRefId` = companyRefId  
✅ **Always**: `Active` != 2 (excludes soft-deleted)  
✅ **If jobId > 0**: `AgentCompanyRefId` = jobId  
✅ **Always**: Sorted by `AgentName` (A-Z)

## Build Status

```
✅ SUCCESS - All 469 files compiled without errors
```

## Security

✅ Requires JWT authentication  
✅ Authorized roles: SUPERADMIN, ADMIN, ROLE_100  
✅ Same security as existing Agent endpoints

## Files Created for Documentation

1. `docs/AGENT_SELECT_ALL_API.md` - Full API documentation
2. `AGENT_API_IMPLEMENTATION.md` - Implementation details
3. `AGENT_COMPANY_FIX.md` - Authorization fix (previously applied)

## Deployment

1. Pull the latest code
2. Run: `mvn clean compile` (already done ✅)
3. Deploy to your server
4. Test using curl/Postman examples above
5. Update frontend to use `/api/agents/select-all` endpoint

## Example Usage in Frontend

### JavaScript/TypeScript
```javascript
async function getAgents(companyId, agentCompanyId = 0) {
  try {
    const params = new URLSearchParams({
      companyRefId: companyId,
      jobId: agentCompanyId
    });
    
    const response = await fetch(`/api/agents/select-all?${params}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
        'Content-Type': 'application/json'
      }
    });
    
    const result = await response.json();
    
    if (result.ok) {
      console.log(`Found ${result.count} agents`);
      console.log(result.data); // Array of agents
    } else {
      console.error(result.error);
    }
  } catch (error) {
    console.error('Request failed:', error);
  }
}

// Usage
getAgents(5);        // Get all agents for company 5
getAgents(5, 3);     // Get agents for company 5, filtered by agent company 3
```

---

**Need more details?** See `docs/AGENT_SELECT_ALL_API.md`  
**Need authorization fix info?** See `AGENT_COMPANY_FIX.md`  
**Build successful?** Yes ✅

