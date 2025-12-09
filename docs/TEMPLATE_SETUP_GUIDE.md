# CraftMyPDF Template Setup - Visual Guide

## 🎨 Template Design Example

Below is a recommended certificate template design for LearnNow:

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                     GRADIENT BACKGROUND                              │
│                  ({{primary_color}} → {{secondary_color}})           │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                                                            │    │
│  │                     🎓 LearnNow                            │    │
│  │                                                            │    │
│  │            Certificate of Completion                       │    │
│  │                                                            │    │
│  │  ──────────────────────────────────────────────────────   │    │
│  │                                                            │    │
│  │              This certifies that                           │    │
│  │                                                            │    │
│  │              {{learner_name}}                              │    │
│  │              (Large, bold font)                            │    │
│  │                                                            │    │
│  │        has successfully completed the course               │    │
│  │                                                            │    │
│  │              {{course_name}}                               │    │
│  │              (Medium, bold font)                           │    │
│  │                                                            │    │
│  │              on {{completion_date}}                        │    │
│  │              (Small, regular font)                         │    │
│  │                                                            │    │
│  │  ──────────────────────────────────────────────────────   │    │
│  │                                                            │    │
│  │  Certificate ID: {{certificate_id}}                        │    │
│  │  (Monospace font, small)                                   │    │
│  │                                                            │    │
│  │                                                            │    │
│  │  [Signature Line]        [LearnNow Logo]                   │    │
│  │                                                            │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

## 📝 Step-by-Step CraftMyPDF Template Creation

### Step 1: Login to CraftMyPDF
1. Go to https://craftmypdf.com
2. Click **"Sign In"** or **"Dashboard"**
3. Use the account associated with API key: `d630MjY1Mzk6MjY2OTE6Yk9vRVl0dFRBNzV1YU5VdQ==`

### Step 2: Create New Template
1. Click **"Create Template"** button (top right)
2. Choose one of:
   - **"Blank Canvas"** - Start from scratch
   - **"Certificate Template"** - Start with pre-made layout
   - **"Import Template"** - Upload existing design

### Step 3: Set Page Dimensions
Recommended settings:
- **Page Size**: A4 Landscape (297mm x 210mm) or Letter Landscape (11" x 8.5")
- **Orientation**: Landscape
- **Margins**: 20mm all sides

### Step 4: Add Background

#### Option A: Gradient Background
1. Add **Rectangle** element covering entire page
2. Set **Fill Type** to "Gradient"
3. Configure gradient:
   - **Start Color**: `{{primary_color}}` (use expression)
   - **End Color**: `{{secondary_color}}` (use expression)
   - **Direction**: Top to bottom or diagonal
   - **Opacity**: 0.1 to 0.3 (subtle)

#### Option B: Solid Background with Accent
1. Add **Rectangle** for main background (white or light gray)
2. Add **Rectangle** for top accent bar (use `{{primary_color}}`)
3. Add geometric shapes for decoration (circles, triangles)

### Step 5: Add Header Section

#### LearnNow Logo/Text
1. Add **Text** element at top center
2. Content: `LearnNow` or `🎓 LearnNow`
3. Font: Montserrat Bold or Poppins Bold
4. Size: 28-32pt
5. Color: `{{primary_color}}` or dark gray (#333333)
6. Alignment: Center

#### Certificate Title
1. Add **Text** element below logo
2. Content: `Certificate of Completion`
3. Font: Montserrat Regular or Poppins Regular
4. Size: 20-24pt
5. Color: Dark gray (#666666)
6. Alignment: Center

### Step 6: Add Content Section

#### Introductory Text
1. Add **Text** element
2. Content: `This certifies that`
3. Font: Montserrat Light or Poppins Light
4. Size: 14-16pt
5. Color: Gray (#888888)
6. Alignment: Center

#### Learner Name (IMPORTANT)
1. Add **Text** element
2. Content: `{{learner_name}}` ⚠️ EXACTLY THIS
3. Font: Montserrat Bold or Poppins Bold
4. Size: 32-36pt (largest text)
5. Color: `{{primary_color}}` (use expression)
6. Alignment: Center
7. Text Transform: Uppercase (optional)

#### Course Completion Text
1. Add **Text** element
2. Content: `has successfully completed the course`
3. Font: Montserrat Regular
4. Size: 14-16pt
5. Color: Gray (#888888)
6. Alignment: Center

#### Course Name (IMPORTANT)
1. Add **Text** element
2. Content: `{{course_name}}` ⚠️ EXACTLY THIS
3. Font: Montserrat SemiBold or Poppins SemiBold
4. Size: 24-28pt
5. Color: `{{secondary_color}}` (use expression)
6. Alignment: Center
7. Optional: Add underline or background box

#### Completion Date (IMPORTANT)
1. Add **Text** element
2. Content: `on {{completion_date}}` ⚠️ EXACTLY THIS
3. Font: Montserrat Regular
4. Size: 14pt
5. Color: Gray (#888888)
6. Alignment: Center

### Step 7: Add Certificate ID Section

#### Certificate ID (IMPORTANT)
1. Add **Text** element near bottom
2. Content: `Certificate ID: {{certificate_id}}` ⚠️ EXACTLY THIS
3. Font: Courier New or Monaco (monospace)
4. Size: 10-12pt
5. Color: Gray (#666666)
6. Alignment: Left or Center
7. Optional: Add background box for emphasis

### Step 8: Add Footer Elements

#### Signature Line (Optional)
1. Add **Line** element (horizontal)
2. Width: 150-200px
3. Color: Gray (#CCCCCC)
4. Position: Bottom left

#### Signature Text (Optional)
1. Add **Text** element below line
2. Content: `Authorized Signature`
3. Font: Montserrat Light
4. Size: 10pt
5. Color: Gray (#888888)

#### Logo (Optional)
1. Add **Image** element
2. Upload LearnNow logo
3. Position: Bottom right or top right
4. Size: 80-120px width

### Step 9: Add Decorative Elements (Gen-Z Style)

#### Geometric Shapes
- Add **Circle** elements with `{{primary_color}}` at low opacity (0.1-0.2)
- Add **Triangle** elements for modern look
- Add **Line** elements as dividers
- Position decoratively around content

#### Border/Frame (Optional)
- Add **Rectangle** with no fill, only stroke
- Stroke color: `{{primary_color}}`
- Stroke width: 2-4px
- Rounded corners: 10-15px

### Step 10: Configure Dynamic Colors

For elements using dynamic colors:

1. Select the element
2. Click on **"Property Binding"** tab (right panel)
3. For **Color/Fill Color** property:
   - Click **"Bind to Expression"**
   - Enter: `{{primary_color}}` or `{{secondary_color}}`
   - Click **"Save"**

### Step 11: Test Template with Sample Data

1. Click **"Data"** tab (right panel)
2. Enter sample JSON:
   ```json
   {
     "platform_name": "LearnNow",
     "certificate_title": "Certificate of Completion",
     "learner_name": "Koushal Sharma",
     "course_name": "Python for Data Science",
     "completion_date": "December 9, 2025",
     "certificate_id": "LN-2025-PYTHON-72E2D5",
     "primary_color": "#8b5cf6",
     "secondary_color": "#f59e0b"
   }
   ```
3. Click **"Preview PDF"** button
4. Verify:
   - All fields are populated correctly
   - Colors match the theme
   - Layout looks professional
   - Certificate ID is clearly visible
   - No overlapping text

### Step 12: Save Template and Get ID

1. Click **"Save"** button (top right)
2. Enter template name: `LearnNow Certificate - Gen-Z`
3. Click **"Save"**
4. Copy the **Template ID** from URL:
   - URL: `https://craftmypdf.com/designer/?template_id=05f77b2b18ad809a`
   - Template ID: `05f77b2b18ad809a`

### Step 13: Update LearnNow Configuration

Edit `src/main/resources/application.yml`:

```yaml
certificate:
  template:
    id: 05f77b2b18ad809a  # Your actual template ID
```

## 🎯 Required Fields Checklist

Make sure your template includes ALL these fields:

- [ ] `{{platform_name}}` - Platform name (LearnNow)
- [ ] `{{certificate_title}}` - Certificate title
- [ ] `{{learner_name}}` - Student name (MOST IMPORTANT)
- [ ] `{{course_name}}` - Course name (IMPORTANT)
- [ ] `{{completion_date}}` - Date of completion (IMPORTANT)
- [ ] `{{certificate_id}}` - Unique certificate ID (IMPORTANT)
- [ ] `{{primary_color}}` - Dynamic primary color
- [ ] `{{secondary_color}}` - Dynamic secondary color

## 🎨 Color Theme Examples

### Web Development Theme
```json
{
  "primary_color": "#6366f1",
  "secondary_color": "#8b5cf6"
}
```
**Preview**: Indigo to Purple gradient (modern, tech-focused)

### Python/Data Science Theme
```json
{
  "primary_color": "#8b5cf6",
  "secondary_color": "#f59e0b"
}
```
**Preview**: Purple to Orange gradient (creative, analytical)

### Java/Spring Theme
```json
{
  "primary_color": "#10b981",
  "secondary_color": "#14b8a6"
}
```
**Preview**: Green to Teal gradient (fresh, enterprise)

### JavaScript/React Theme
```json
{
  "primary_color": "#06b6d4",
  "secondary_color": "#3b82f6"
}
```
**Preview**: Cyan to Blue gradient (vibrant, modern)

### Cloud Computing Theme
```json
{
  "primary_color": "#0ea5e9",
  "secondary_color": "#6366f1"
}
```
**Preview**: Sky Blue to Indigo gradient (airy, technological)

## 💡 Design Tips

### Font Recommendations
- **Headers**: Montserrat Bold, Poppins Bold, Raleway Bold
- **Body**: Montserrat Regular, Poppins Regular, Open Sans
- **Monospace**: Courier New, Monaco, Source Code Pro

### Color Psychology
- **Indigo/Purple**: Innovation, creativity, technology
- **Green/Teal**: Growth, success, achievement
- **Blue/Cyan**: Trust, professionalism, knowledge
- **Orange**: Energy, enthusiasm, creativity

### Layout Best Practices
- Use **center alignment** for main content (names, titles)
- Keep **margins** consistent (20-30mm)
- Use **white space** generously
- **Hierarchy**: Name > Course Name > Other text
- **Contrast**: Ensure text is readable on background

### Gen-Z Aesthetic
- **Gradients** instead of solid colors
- **Rounded corners** on shapes and borders
- **Minimalist** - less is more
- **Bold typography** for impact
- **Geometric patterns** as accents
- **High contrast** for readability
- **Modern fonts** (avoid serif unless intentional)

## 🔧 Troubleshooting

### Issue: Fields not populating
**Solution**: Check field names EXACTLY match (case-sensitive):
- ✅ `{{learner_name}}` 
- ❌ `{{learnerName}}` or `{{Learner_Name}}`

### Issue: Colors not working
**Solution**: Use expression binding, not static text:
1. Select element
2. Property Binding → Color
3. Enter: `{{primary_color}}` (with double braces)

### Issue: Certificate looks bad
**Solution**: Test with all 5 color themes to ensure design works with any color combination

### Issue: Text overlapping
**Solution**: Use relative positioning and ensure elements have enough spacing

## 📤 Export Template (Optional)

To backup your template:
1. Click **"Settings"** tab
2. Click **"Export Template"**
3. Save JSON file
4. Import later via **"Import Template"**

## 🔄 Template Versions

CraftMyPDF supports template versioning:
- Each save creates a new version
- API uses latest version by default
- Can specify version in API call (advanced)

## ✅ Final Checklist

Before using template in production:

- [ ] All 8 required fields added to template
- [ ] Tested with sample data from all 5 color themes
- [ ] PDF preview looks professional and readable
- [ ] Certificate ID is clearly visible
- [ ] Learner name is prominent
- [ ] Course name stands out
- [ ] Colors are applied correctly via expressions
- [ ] No overlapping or cut-off text
- [ ] Template saved successfully
- [ ] Template ID copied and configured in application.yml
- [ ] Backend restarted with new configuration
- [ ] Test certificate generated successfully

---

**Need Help?**
- CraftMyPDF Documentation: https://craftmypdf.com/docs
- CraftMyPDF Support: hello@craftmypdf.com
- Template Editor Guide: https://craftmypdf.com/docs/template-editor.html
