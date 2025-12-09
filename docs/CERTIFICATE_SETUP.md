# Certificate System Setup Guide

## Overview
LearnNow uses **CraftMyPDF** API to generate professional, branded certificates with unique IDs for course completions.

## Certificate Features
- ✅ Unique Certificate IDs: `LN-{YEAR}-{COURSE_ABBR}-{RANDOM_6}` (e.g., `LN-2025-PYTHON-72E2D5`)
- ✅ LearnNow branding with Gen-Z friendly design
- ✅ Course-specific color themes (5 themes: Web Dev, Java, Python/Data Science, JavaScript, Cloud)
- ✅ Downloadable PDF certificates stored on CDN
- ✅ LinkedIn-shareable certificates
- ✅ Public verification system
- ✅ 1-year expiration on CDN

## Setup Instructions

### Step 1: Create CraftMyPDF Account
1. Go to [CraftMyPDF](https://craftmypdf.com)
2. Sign up for a free account
3. Your API key is already configured: `d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==`

### Step 2: Create Certificate Template
1. Log in to CraftMyPDF dashboard
2. Click **"Create Template"** or **"New Template"**
3. Choose **"Blank Canvas"** or **"Certificate Template"**

### Step 3: Design Your Certificate Template
Use the following fields in your template design:

#### Required Fields (these will be auto-populated):
- `{{platform_name}}` - Will show "LearnNow"
- `{{certificate_title}}` - Will show "Certificate of Completion"
- `{{learner_name}}` - Student's full name
- `{{course_name}}` - Course name (e.g., "Python for Beginners")
- `{{completion_date}}` - Format: "December 9, 2025"
- `{{certificate_id}}` - Unique ID (e.g., "LN-2025-PYTHON-72E2D5")

#### Color Variables (for dynamic theming):
- `{{primary_color}}` - Primary brand color (hex)
- `{{secondary_color}}` - Secondary accent color (hex)

#### Design Recommendations:
```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    🎓 LearnNow                              │
│                                                             │
│           Certificate of Completion                         │
│                                                             │
│        This certifies that                                  │
│                                                             │
│        [learner_name]                                       │
│                                                             │
│        has successfully completed                           │
│                                                             │
│        [course_name]                                        │
│                                                             │
│        on [completion_date]                                 │
│                                                             │
│        Certificate ID: [certificate_id]                     │
│                                                             │
│        [Signature Line]                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Gen-Z Friendly Design Tips:**
- Use gradient backgrounds with `primary_color` → `secondary_color`
- Modern sans-serif fonts (Montserrat, Poppins, Inter)
- Minimalist design with clean lines
- Add subtle geometric shapes or patterns
- Include QR code pointing to verification URL (optional)

### Step 4: Get Template ID
1. After saving your template, click on it in the dashboard
2. Copy the **Template ID** (format: `05f77b2b18ad809a`)
3. It will be in the URL: `https://craftmypdf.com/designer/?template_id=YOUR_TEMPLATE_ID`

### Step 5: Configure LearnNow Backend
Update `src/main/resources/application.yml`:

```yaml
certificate:
  template:
    id: YOUR_TEMPLATE_ID  # Replace with your actual template ID
```

Or set as environment variable:
```bash
CERTIFICATE_TEMPLATE_ID=05f77b2b18ad809a
```

### Step 6: Test Certificate Generation
1. Rebuild and restart backend:
   ```bash
   .\mvnw.cmd clean package -DskipTests
   java -jar target\LEARNnow-0.0.0.jar
   ```

2. Complete a course in the frontend
3. Click **"Generate Certificate"** button
4. Verify the certificate is generated and displayed

## Course Color Themes

The system automatically applies color themes based on course names:

| Course Type | Keywords | Primary Color | Secondary Color |
|------------|----------|---------------|-----------------|
| **Web Development** | web, html, css, frontend | Indigo (#6366f1) | Purple (#8b5cf6) |
| **Java/Spring** | java, spring | Green (#10b981) | Teal (#14b8a6) |
| **Python/Data Science** | python, data, ml | Purple (#8b5cf6) | Orange (#f59e0b) |
| **JavaScript/React** | react, node, javascript | Cyan (#06b6d4) | Blue (#3b82f6) |
| **Cloud** | cloud, aws, azure | Sky Blue (#0ea5e9) | Indigo (#6366f1) |
| **Default** | (all others) | Indigo (#6366f1) | Purple (#8b5cf6) |

## API Configuration

### Current Setup:
- **API Endpoint**: `https://api.craftmypdf.com`
- **API Key**: `d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==`
- **Regional Endpoint**: Default (Singapore) - can be changed to EU/US/AU
- **Export Type**: JSON (certificate stored on CDN)
- **Expiration**: 525,600 minutes (1 year)

### Regional Endpoints (Optional):
```yaml
# Europe (Frankfurt)
certificate.api.url: https://api-de.craftmypdf.com

# US East (N. Virginia)
certificate.api.url: https://api-us.craftmypdf.com

# Australia (Sydney)
certificate.api.url: https://api-au.craftmypdf.com
```

## Certificate Workflow

### 1. User Completes Course
- Frontend checks: `GET /api/certificates/check/{userId}/{courseId}`
- Returns: `{"hasCertificate": false}`

### 2. User Clicks "Generate Certificate"
- Frontend calls: `POST /api/certificates/generate`
- Payload:
  ```json
  {
    "userId": "user_36WH3Kl3FM9Mp6HhQWYRL23QDF8",
    "courseId": 21,
    "userName": "Koushal Sharma"
  }
  ```

### 3. Backend Process
1. Check for existing certificate (prevent duplicates)
2. Fetch course details from database
3. Generate unique certificate ID: `LN-2025-PYTHON-72E2D5`
4. Determine color theme based on course name
5. Call CraftMyPDF API:
   ```json
   {
     "template_id": "YOUR_TEMPLATE_ID",
     "export_type": "json",
     "expiration": 525600,
     "data": {
       "platform_name": "LearnNow",
       "certificate_title": "Certificate of Completion",
       "learner_name": "Koushal Sharma",
       "course_name": "Python for Beginners",
       "completion_date": "December 9, 2025",
       "certificate_id": "LN-2025-PYTHON-72E2D5",
       "primary_color": "#8b5cf6",
       "secondary_color": "#f59e0b"
     }
   }
   ```
6. Receive certificate URL from CraftMyPDF
7. Save certificate to database
8. Return certificate DTO to frontend

### 4. Frontend Displays Certificate
- Show success modal with certificate ID
- Display "Download Certificate" button
- Certificate appears in Profile > Credentials tab
- User can download PDF or share to LinkedIn

## Database Schema

```sql
CREATE TABLE certificate (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  certificate_number VARCHAR(50) UNIQUE NOT NULL,
  certificate_url VARCHAR(500) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  course_id BIGINT NOT NULL,
  course_name VARCHAR(255) NOT NULL,
  user_name VARCHAR(255) NOT NULL,
  issued_at TIMESTAMP NOT NULL,
  completion_date TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_course (user_id, course_id),
  INDEX idx_cert_number (certificate_number)
);
```

## API Endpoints

### Generate Certificate
```http
POST /api/certificates/generate
Content-Type: application/json

{
  "userId": "string",
  "courseId": 123,
  "userName": "string"
}
```

### Get User Certificates
```http
GET /api/certificates/user/{userId}
```

### Verify Certificate
```http
GET /api/certificates/verify/{certificateNumber}
```

### Check if Certificate Exists
```http
GET /api/certificates/check/{userId}/{courseId}
```

## Troubleshooting

### Issue: "Template ID not configured"
**Solution**: Update `application.yml` with your CraftMyPDF template ID

### Issue: "CraftMyPDF API returned error"
**Possible causes**:
- Invalid API key → Check API key in CraftMyPDF dashboard
- Invalid template ID → Verify template exists
- Template fields mismatch → Ensure all required fields are in template
- Rate limit exceeded → Wait and retry

**Check logs**: Look for `[CraftMyPDF]` messages in backend console

### Issue: "Certificate shows mock URL"
**Solution**: This is the fallback when CraftMyPDF API fails. Check:
1. API key is correct
2. Template ID is valid
3. Backend can reach `https://api.craftmypdf.com`
4. No firewall blocking API requests

### Issue: "Certificate not appearing in Profile"
**Solution**:
1. Check browser console for errors
2. Verify certificate was saved: Check database `certificate` table
3. Ensure user ID matches between generation and profile fetch
4. Check backend logs for database errors

## Production Considerations

### 1. Environment Variables
Set these in production:
```bash
CERTIFICATE_API_URL=https://api.craftmypdf.com
CERTIFICATE_API_KEY=your-production-api-key
CERTIFICATE_TEMPLATE_ID=your-production-template-id
```

### 2. Error Handling
- Current implementation has fallback to mock URLs
- Consider implementing retry logic for API failures
- Add email notifications for failed certificate generations

### 3. Storage Options
**Current**: CraftMyPDF CDN (1 year expiration)

**Alternative**: Upload to your own S3 bucket
```yaml
certificate:
  storage:
    type: s3
    bucket: learnnow-certificates
    region: us-east-1
```

Configure in CraftMyPDF:
- Post Action → AWS S3
- Enter S3 credentials
- Set `cloud_storage: 0` in API payload
- Set `postaction_s3_filekey` and `postaction_s3_bucket`

### 4. Monitoring
Add monitoring for:
- Certificate generation success rate
- CraftMyPDF API response times
- Failed generation alerts
- Certificate verification requests

### 5. Scaling
- CraftMyPDF has rate limits (100 requests per 10 seconds)
- For high volume, consider async generation:
  - Use `/v1/create-async` endpoint
  - Implement webhook handler
  - Update certificate URL after generation

## Sample Certificate Data

### Web Development Course
```json
{
  "learner_name": "Koushal Sharma",
  "course_name": "Complete Web Development Bootcamp",
  "completion_date": "December 9, 2025",
  "certificate_id": "LN-2025-WEBDEV-A3F8E2",
  "primary_color": "#6366f1",
  "secondary_color": "#8b5cf6"
}
```

### Python Course
```json
{
  "learner_name": "Koushal Sharma",
  "course_name": "Python for Data Science",
  "completion_date": "December 9, 2025",
  "certificate_id": "LN-2025-PYTHON-72E2D5",
  "primary_color": "#8b5cf6",
  "secondary_color": "#f59e0b"
}
```

## Next Steps

1. ✅ Create CraftMyPDF template with required fields
2. ✅ Copy template ID to `application.yml`
3. ✅ Rebuild and restart backend
4. ✅ Test certificate generation
5. ⏸️ Add real video completion tracking (currently simplified)
6. ⏸️ Implement certificate email delivery
7. ⏸️ Add certificate download analytics
8. ⏸️ Create public certificate verification page

## Resources

- [CraftMyPDF Documentation](https://craftmypdf.com/docs)
- [CraftMyPDF API Reference](https://craftmypdf.com/docs/index.html)
- [CraftMyPDF Template Editor](https://craftmypdf.com/designer/)
- [LearnNow Certificate Implementation](../src/main/java/me/learn/now/service/CertificateService.java)
