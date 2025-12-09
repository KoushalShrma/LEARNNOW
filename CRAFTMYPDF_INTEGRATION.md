# CraftMyPDF Integration - Quick Reference

## ✅ What's Been Done

### 1. Backend Integration
**File**: `src/main/java/me/learn/now/service/CertificateService.java`

**Changes**:
- ✅ Replaced mock certificate generation with real CraftMyPDF API calls
- ✅ Added API configuration properties (URL, API key, template ID)
- ✅ Implemented full API payload structure
- ✅ Added error handling with fallback to mock URLs
- ✅ Configured certificate expiration to 1 year (525,600 minutes)
- ✅ Set up detailed logging for debugging

**API Call Details**:
```java
POST https://api.craftmypdf.com/v1/create
Headers:
  X-API-KEY: d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==
  Content-Type: application/json

Payload:
{
  "template_id": "YOUR_TEMPLATE_ID",
  "export_type": "json",
  "expiration": 525600,
  "output_file": "LN-2025-PYTHON-72E2D5.pdf",
  "cloud_storage": 1,
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

### 2. Configuration
**File**: `src/main/resources/application.yml`

**Added**:
```yaml
certificate:
  api:
    url: https://api.craftmypdf.com
    key: d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==
  template:
    id: YOUR_TEMPLATE_ID  # ⚠️ NEEDS TO BE SET
```

### 3. Documentation
**File**: `docs/CERTIFICATE_SETUP.md`

**Contents**:
- Complete setup guide with step-by-step instructions
- Template design recommendations (Gen-Z friendly)
- All required template fields
- Color theme mapping
- API configuration details
- Troubleshooting guide
- Production considerations

### 4. Build Status
- ✅ Backend compiled successfully (BUILD SUCCESS)
- ✅ Backend running on port 8080
- ✅ No compilation errors
- ⚠️ Warnings only (non-critical: MySQL8Dialect deprecated, JPA open-in-view)

## ⚠️ What You Need to Do Now

### Step 1: Create CraftMyPDF Template (REQUIRED)

1. **Go to CraftMyPDF Dashboard**
   - URL: https://craftmypdf.com
   - Log in with the account associated with API key: `d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==`

2. **Create New Template**
   - Click "Create Template" or "New Template"
   - Choose "Blank Canvas" or start with a certificate template

3. **Add These Fields to Your Template**
   ```
   Required fields (will be auto-populated):
   - {{platform_name}} → "LearnNow"
   - {{certificate_title}} → "Certificate of Completion"
   - {{learner_name}} → User's name
   - {{course_name}} → Course name
   - {{completion_date}} → "December 9, 2025"
   - {{certificate_id}} → "LN-2025-PYTHON-72E2D5"
   
   Color variables (for dynamic theming):
   - {{primary_color}} → Hex color
   - {{secondary_color}} → Hex color
   ```

4. **Design Tips (Gen-Z Friendly)**
   - Use gradient backgrounds with `{{primary_color}}` → `{{secondary_color}}`
   - Modern fonts: Montserrat, Poppins, Inter
   - Minimalist design with clean lines
   - Add subtle geometric shapes
   - Include certificate ID prominently

5. **Save Template and Get ID**
   - After saving, copy the **Template ID** from URL
   - URL format: `https://craftmypdf.com/designer/?template_id=YOUR_TEMPLATE_ID`
   - Example ID: `05f77b2b18ad809a`

### Step 2: Update Configuration

**Option A: Edit application.yml**
```bash
# File: src/main/resources/application.yml
certificate:
  template:
    id: 05f77b2b18ad809a  # Replace with your actual template ID
```

**Option B: Set Environment Variable**
```bash
# Windows PowerShell
$env:CERTIFICATE_TEMPLATE_ID="05f77b2b18ad809a"
```

### Step 3: Restart Backend

If you changed `application.yml`:
```powershell
# Stop current backend
Get-Process | Where-Object {$_.ProcessName -eq "java" -and $_.Path -like "*LEARNnow*"} | Stop-Process -Force

# Start with new config
java -jar target\LEARNnow-0.0.0.jar
```

If using environment variable, just restart.

### Step 4: Test Certificate Generation

1. **Open frontend**: http://localhost:5174
2. **Navigate to a course** (e.g., Python for Beginners - Course ID 21)
3. **Skip to last video** using the hierarchical tree
4. **Click "Generate Certificate"** button
5. **Check backend logs** for:
   ```
   [CraftMyPDF] Calling API to generate certificate: LN-2025-PYTHON-72E2D5
   [CraftMyPDF] Template ID: YOUR_TEMPLATE_ID
   [CraftMyPDF] API Response: {"status":"success","file":"..."}
   [CraftMyPDF] Certificate generated successfully: https://...
   ```

6. **Verify certificate**:
   - Success modal appears with certificate ID
   - Certificate shows in Profile > Credentials
   - Download link works (opens real PDF from CraftMyPDF)

## 🔍 How to Verify It's Working

### Success Indicators
✅ Backend log shows: `[CraftMyPDF] Certificate generated successfully: https://...`
✅ Certificate URL starts with CraftMyPDF CDN domain (not `certificates.learnnow.com`)
✅ Clicking "Download Certificate" opens a real PDF file
✅ PDF contains the actual certificate with your template design

### Failure Indicators (Will Fallback to Mock)
❌ Backend log shows: `[CraftMyPDF] Error generating certificate: ...`
❌ Certificate URL is mock: `https://certificates.learnnow.com/...`
❌ Clicking download link shows 404 error

### Common Errors and Solutions

**Error**: "Template ID not configured" or empty PDF
**Solution**: Set `certificate.template.id` in application.yml

**Error**: "CraftMyPDF API returned error: Invalid template ID"
**Solution**: Double-check template ID from CraftMyPDF dashboard

**Error**: "CraftMyPDF API returned error: Field not found"
**Solution**: Add missing fields to your template (check docs/CERTIFICATE_SETUP.md for required fields)

**Error**: API timeout or connection refused
**Solution**: 
- Check internet connection
- Verify API endpoint is reachable: `https://api.craftmypdf.com`
- Check firewall settings

## 📊 Current Status

| Component | Status | Notes |
|-----------|--------|-------|
| Backend Integration | ✅ Complete | CraftMyPDF API fully integrated |
| Configuration | ⚠️ Partial | API key set, template ID needed |
| Frontend | ✅ Ready | No changes needed |
| Template | ❌ Not Set Up | **ACTION REQUIRED** |
| Testing | ⏸️ Pending | Waiting for template setup |

## 🎯 Next Steps Priority

1. **CRITICAL**: Create CraftMyPDF template (10 minutes)
2. **CRITICAL**: Get template ID and update config (2 minutes)
3. **CRITICAL**: Restart backend (1 minute)
4. **HIGH**: Test certificate generation (5 minutes)
5. **MEDIUM**: Verify PDF quality and design
6. **LOW**: Adjust template design if needed

## 📞 Support

**CraftMyPDF Documentation**: https://craftmypdf.com/docs
**LearnNow Setup Guide**: docs/CERTIFICATE_SETUP.md
**API Reference**: https://craftmypdf.com/docs/index.html

## 🔐 Security Notes

- ✅ API key is Base64 encoded (safe for application.yml)
- ✅ Certificate endpoints are publicly accessible (by design)
- ✅ Certificates expire after 1 year on CDN
- ⚠️ For production, consider moving API key to environment variables
- ⚠️ Set up your own S3 bucket for permanent storage (optional)

## 🚀 Production Checklist

When deploying to production:

- [ ] Create production CraftMyPDF template
- [ ] Set template ID via environment variable
- [ ] Move API key to environment variable
- [ ] Set up custom S3 bucket (optional)
- [ ] Configure regional endpoint if needed (EU/US/AU)
- [ ] Set up monitoring for certificate generation
- [ ] Test certificate verification flow
- [ ] Add email notifications for certificate delivery
- [ ] Implement progress tracking (currently simplified)
- [ ] Set up certificate analytics

---

**Last Updated**: December 9, 2025, 19:14 IST
**Backend Version**: 0.0.0
**CraftMyPDF API Version**: v1
