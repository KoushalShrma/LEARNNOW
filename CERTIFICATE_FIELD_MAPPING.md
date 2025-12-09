# Certificate Template Field Mapping

## Template Information
- **Template ID**: `af977b23db7efa1c`
- **Template Style**: Classic certificate with decorative borders
- **Platform**: CraftMyPDF

## Field Mapping

Based on your certificate template, here's how LearnNow data maps to the template fields:

| Template Field | LearnNow Data | Example Value |
|----------------|---------------|---------------|
| `title` | **Course name** | "Python for Beginners" |
| `proudly_to` | Static text | "Proudly Presented To" |
| `recipient` | **Logged-in user's name** | "John Doe" |
| `desc` | Description | "has successfully completed the course" |
| `date` | **Real-time completion date** | "December 9, 2025" |
| `signature` | **CEO signature** | "Koushal Sharma" |
| `signer_name` | **CEO title** | "CEO & Founder, LearnNow" |

## Template Structure (from image)

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│                {{ data.title }}                          │
│             (Python for Beginners)                       │
│                                                          │
│              Proudly Presented To                        │
│                                                          │
│              {{ data.recipient }}                        │
│                  (John Doe)                              │
│                                                          │
│            {{ data.desc }}                               │
│     (has successfully completed the course)              │
│                                                          │
│              {{ data.date }}                             │
│            (December 9, 2025)                            │
│                                                          │
│         {{ data.signature }}                             │
│           (Koushal Sharma)                               │
│                                                          │
│       {{ data.signer_name }}                             │
│      (CEO & Founder, LearnNow)                           │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

## API Payload Example

When generating a certificate, the backend sends:

```json
{
  "template_id": "af977b23db7efa1c",
  "export_type": "json",
  "expiration": 525600,
  "output_file": "LN-2025-PYTHON-72E2D5.pdf",
  "cloud_storage": 1,
  "data": {
    "title": "Python for Beginners",
    "proudly_to": "Proudly Presented To",
    "recipient": "John Doe",
    "desc": "has successfully completed the course",
    "date": "December 9, 2025",
    "signature": "Koushal Sharma",
    "signer_name": "CEO & Founder, LearnNow"
  }
}
```

## Expected Response

```json
{
  "status": "success",
  "file": "https://craftmypdf-gen.s3.ap-southeast-1.amazonaws.com/2025/12/LN-2025-PYTHON-72E2D5.pdf",
  "transaction_ref": "a0430897-2c94-40e1-a09b-57403d811ceb"
}
```

## Implementation Details

### Certificate Number Format
- Pattern: `LN-{YEAR}-{COURSE_ABBR}-{RANDOM_6}`
- Example: `LN-2025-PYTHON-72E2D5`
- Where:
  - `LN` = LearnNow
  - `2025` = Current year
  - `PYTHON` = Course abbreviation (first letters of words)
  - `72E2D5` = Random 6-character hex from UUID

### Course Abbreviation Logic
```java
// "Python for Beginners" → "PYTHON"
// "Complete Web Development Bootcamp" → "CWDB"
// "Spring Boot Masterclass" → "SBM"
```

### Date Format
- Pattern: `MMMM d, yyyy`
- Examples:
  - "December 9, 2025"
  - "January 15, 2026"
  - "March 3, 2025"

## Testing

To test the certificate generation:

1. **Navigate to a course** in the frontend
2. **Complete the course** (currently: skip to last video)
3. **Click "Generate Certificate"** button
4. **Check backend logs** for:
   ```
   [CraftMyPDF] Calling API to generate certificate: LN-2025-PYTHON-72E2D5
   [CraftMyPDF] Template ID: af977b23db7efa1c
   [CraftMyPDF] API Response: {"status":"success",...}
   [CraftMyPDF] Certificate generated successfully: https://...
   ```

5. **Verify in frontend**:
   - Success modal appears
   - Certificate ID displayed
   - Download button works
   - Certificate appears in Profile > Credentials

## Troubleshooting

### If you see "Template ID: YOUR_TEMPLATE_ID"
**Problem**: Old backend still running
**Solution**: 
```powershell
Get-Process | Where-Object {$_.ProcessName -eq "java"} | Stop-Process -Force
.\mvnw.cmd clean package -DskipTests
java -jar target\LEARNnow-0.0.0.jar
```

### If you see "400 Bad Request"
**Problem**: Field name mismatch or missing required field
**Solution**: Check that your template has all fields:
- `title`
- `proudly_to`
- `recipient`
- `desc`
- `date`
- `signature`
- `signer_name`

### If certificate URL is mock
**Problem**: API call failed, fallback activated
**Symptoms**: URL starts with `https://certificates.learnnow.com/`
**Solution**: Check backend logs for error details

## Current Status

✅ **Template ID**: Configured (`af977b23db7efa1c`)
✅ **Field Mapping**: Implemented
✅ **Backend**: Rebuilt and running
✅ **API Key**: Configured
⏸️ **Testing**: Ready for certificate generation test

## Next Steps

1. Open frontend: http://localhost:5174
2. Navigate to any course
3. Click through to last video
4. Click "Generate Certificate"
5. Verify PDF is generated and downloadable

---

**Last Updated**: December 9, 2025, 19:40 IST
**Template ID**: af977b23db7efa1c
**Backend Status**: Running on port 8080
