# SelectJobAllData API Documentation

## Overview
This API endpoint replaces the C# Dapper-based `SelectJobAllData` method with a Spring Boot JPA implementation. It returns combined job details and job status details for a specific company and job with all related names from master tables.

## Endpoint
**POST** `/api/job-all-data/select`

## Request Parameters
```
Query Parameters:
  - companyId: Integer (required) - The Company Reference ID
  - jobId: Integer (required) - The Job Master Reference ID
```

## Request Example
```bash
curl -X POST "http://localhost:8080/api/job-all-data/select?companyId=5&jobId=3" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
```

## Success Response (200 OK)
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Job Data Retrieved Successfully",
  "data": {
    "jobTypeDetails": [
      {
        "id": 1,
        "jobMasterRefId": 3,
        "description": "Job Description 1",
        "jobName": "Manager",
        "statusName": "Active",
        "active": 1,
        "mandatory": 1,
        "status": 1
      },
      {
        "id": 2,
        "jobMasterRefId": 3,
        "description": "Job Description 2",
        "jobName": "Manager",
        "statusName": "In Progress",
        "active": 1,
        "mandatory": 0,
        "status": 2
      }
    ],
    "jobStatusDetails": [
      {
        "id": 1,
        "jobMasterRefId": 3,
        "status": 1,
        "statusName": "Active",
        "minStatus": 0,
        "minStatusName": "Pending",
        "sort": 1
      },
      {
        "id": 2,
        "jobMasterRefId": 3,
        "status": 2,
        "statusName": "In Progress",
        "minStatus": 1,
        "minStatusName": "Active",
        "sort": 2
      }
    ]
  },
  "meta": null,
  "error": null
}
```

## Not Found Response (404 Not Found)
```json
{
  "success": false,
  "statusCode": 404,
  "message": "No Job Data Found",
  "data": null,
  "meta": null,
  "error": null
}
```

## Bad Request Response (400 Bad Request)
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Company ID and Job ID must be greater than 0",
  "data": null,
  "meta": null,
  "error": null
}
```

## Error Response (500 Internal Server Error)
```json
{
  "success": false,
  "statusCode": 500,
  "message": "Failed to retrieve job data",
  "data": null,
  "meta": null,
  "error": null
}
```

## Data Structure

### JobTypeDetails (from JobDetails)
- `id`: JobDetails record ID
- `jobMasterRefId`: Reference to JobTypeMaster
- `description`: Job description
- `jobName`: Name from JobTypeMaster.name (via join)
- `statusName`: Name from JobStatusMaster.name (via join on Status field)
- `active`: Active flag (1 = active)
- `mandatory`: Mandatory flag
- `status`: Status reference ID (links to JobStatusMaster)

### JobStatusDetails (from JobStatusDetails)
- `id`: JobStatusDetails record ID
- `jobMasterRefId`: Reference to JobTypeMaster
- `status`: Status ID (links to JobStatusMaster)
- `statusName`: Name from JobStatusMaster.name (via join on Status field)
- `minStatus`: Minimum Status ID (links to JobStatusMaster)
- `minStatusName`: Name from JobStatusMaster.name (via join on MinStatus field)
- `sort`: Sort order

## SQL Equivalent (Original C# Code)
```sql
-- JobDetails with joins
SELECT JD.Id, JD.JobMasterRefId, JD.Description, JD.Active, 
       JM.Name as JobName, JD.Mandatory, JD.Status, JSM.Name as StatusName
FROM JobDetails JD WITH (NOLOCK)
LEFT JOIN JobTypeMaster JM WITH (NOLOCK) ON JM.id = JD.JobMasterRefId
LEFT JOIN JobStatusMaster JSM WITH (NOLOCK) ON JSM.id = JD.Status
WHERE JD.CompanyRefId = :companyId 
  AND JD.JobMasterRefId = :jobId 
  AND JD.Active = 1

-- JobStatusDetails with joins
SELECT JD.Id, JD.JobMasterRefId, JD.Status, JD.MinStatus, JD.Sort,
       ISNULL(JSM.Name, '') as StatusName, ISNULL(JSM1.Name, '') as MinStatusName
FROM JobStatusDetails JD WITH (NOLOCK)
LEFT JOIN JobTypeMaster JM WITH (NOLOCK) ON JM.id = JD.JobMasterRefId
LEFT JOIN JobStatusMaster JSM WITH (NOLOCK) ON JSM.id = JD.Status
LEFT JOIN JobStatusMaster JSM1 WITH (NOLOCK) ON JSM1.id = JD.MinStatus
WHERE JD.CompanyRefId = :companyId 
  AND JD.JobMasterRefId = :jobId
ORDER BY JD.Sort
```

## JPQL Query Used

### JobDetails Query
```
SELECT NEW my.maleva.api.dto.JobDetailsWithNameDto(
  jd.id, jd.jobMasterRefId, jd.description, jm.name, jsm.name, jd.active, jd.mandatory, jd.status)
FROM JobDetails jd
LEFT JOIN JobTypeMaster jm ON jm.id = jd.jobMasterRefId
LEFT JOIN JobStatusMaster jsm ON jsm.id = jd.status
WHERE jd.companyRefId = :companyId
  AND jd.jobMasterRefId = :jobId
  AND jd.active = 1
```

### JobStatusDetails Query
```
SELECT NEW my.maleva.api.dto.JobStatusDetailsWithNameDto(
  jsd.id, jsd.jobMasterRefId, jsd.status, jsm.name, jsd.minStatus, jsm2.name, jsd.sort)
FROM JobStatusDetails jsd
LEFT JOIN JobStatusMaster jsm ON jsm.id = jsd.status
LEFT JOIN JobStatusMaster jsm2 ON jsm2.id = jsd.minStatus
WHERE jsd.companyRefId = :companyId
  AND jsd.jobMasterRefId = :jobId
ORDER BY jsd.sort ASC
```

## Implementation Details

### Files Created/Modified

#### Created DTOs
1. `JobDetailsWithNameDto.java` - Mapped to JobDetails with joined names
2. `JobStatusDetailsWithNameDto.java` - Mapped to JobStatusDetails with joined names
3. `JobTypeAllDataDto.java` - Wrapper DTO for combined response

#### Modified Repositories
1. `JobDetailsRepository.java` - Added `findJobDetailsWithNames()` method with JPQL query
2. `JobStatusDetailsRepository.java` - Added `findJobStatusDetailsWithNames()` method with JPQL query

#### Created Service
1. `JobTypeAllDataService.java` - Service layer that calls both repositories and combines results

#### Created Controller
1. `JobTypeAllDataController.java` - REST endpoint that calls service and returns ApiResponse

## Migration Notes
- This endpoint returns data wrapped in the standard `ApiResponse` object used throughout the Maleva API
- The endpoint uses JPA/JPQL queries instead of raw SQL (Dapper)
- Input validation is performed on both companyId and jobId
- All LEFT JOINs preserve original behavior (returns null names if joins don't match)
- Results are automatically sorted by sort order for JobStatusDetails
- Only returns JobDetails with active = 1 status

## Security
- Requires one of the following roles: ROLE_SUPRERADMIN, ROLE_ADMIN, ROLE_100
- Protected by Spring Security PreAuthorize annotation

