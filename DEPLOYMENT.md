# LEARNnow Deployment Guide 🚀

This guide covers deploying LEARNnow to the cloud using **Render** (backend) and **Vercel/Netlify** (frontend).

---

## Architecture

```
┌─────────────────┐     HTTPS      ┌─────────────────┐
│   Frontend      │ ◄────────────► │   Backend       │
│   (Vercel/      │                │   (Render)      │
│    Netlify)     │                │   Spring Boot   │
└─────────────────┘                └────────┬────────┘
                                            │
                                            ▼
                                   ┌─────────────────┐
                                   │   Database      │
                                   │   (MySQL/       │
                                   │    PlanetScale) │
                                   └─────────────────┘
```

---

## Step 1: Database Setup (Choose One)

### Option A: PlanetScale (Recommended - Free Tier)
1. Go to [planetscale.com](https://planetscale.com)
2. Create a new database named `learnnow`
3. Get the connection string from "Connect" → "Connect with Java"
4. Save the connection URL

### Option B: Railway MySQL
1. Go to [railway.app](https://railway.app)
2. Create a new project → Add MySQL
3. Copy the `DATABASE_URL` from the Variables tab

---

## Step 2: Deploy Backend to Render

### 2.1 Push Code to GitHub
```bash
git add .
git commit -m "Add deployment configuration"
git push origin main
```

### 2.2 Create Render Account & Deploy
1. Go to [render.com](https://render.com) and sign up
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repo
4. Configure:
   - **Name**: `learnnow-api`
   - **Region**: Choose closest to your users
   - **Branch**: `main`
   - **Root Directory**: `.` (leave empty)
   - **Runtime**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/LEARNnow-0.0.0.jar`

### 2.3 Set Environment Variables
In Render Dashboard → Environment → Add these:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `PORT` | `8080` |
| `DATABASE_URL` | `jdbc:mysql://your-db-host:3306/learnnow` |
| `DATABASE_USERNAME` | `your-db-username` |
| `DATABASE_PASSWORD` | `your-db-password` |
| `YOUTUBE_API_KEY` | `your-youtube-api-key` |
| `GROQ_API_KEY` | `your-groq-api-key` |
| `JWT_SECRET` | `your-super-secret-jwt-key-min-32-chars` |
| `CORS_ORIGINS` | `https://your-frontend.vercel.app` |

### 2.4 Deploy
Click **"Create Web Service"** - Render will build and deploy automatically.

**Backend URL**: `https://learnnow-api.onrender.com`

---

## Step 3: Deploy Frontend

### Option A: Vercel (Recommended)

1. Go to [vercel.com](https://vercel.com) and sign up
2. Click **"Add New"** → **"Project"**
3. Import your GitHub repo
4. Configure:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`

5. Add Environment Variables:
   | Key | Value |
   |-----|-------|
   | `VITE_API_BASE_URL` | `https://learnnow-api.onrender.com` |

6. Click **"Deploy"**

**Frontend URL**: `https://learnnow.vercel.app`

---

### Option B: Netlify

1. Go to [netlify.com](https://netlify.com) and sign up
2. Click **"Add new site"** → **"Import an existing project"**
3. Connect GitHub and select your repo
4. Configure:
   - **Base directory**: `frontend`
   - **Build command**: `npm run build`
   - **Publish directory**: `frontend/dist`

5. Go to **Site settings** → **Environment variables**:
   | Key | Value |
   |-----|-------|
   | `VITE_API_BASE_URL` | `https://learnnow-api.onrender.com` |

6. Trigger redeploy

**Frontend URL**: `https://learnnow.netlify.app`

---

## Step 4: Update CORS Origins

After deploying frontend, update the backend's `CORS_ORIGINS`:

1. Go to Render Dashboard → Your service → Environment
2. Update `CORS_ORIGINS` to your actual frontend URL:
   ```
   https://learnnow-yourname.vercel.app
   ```

---

## Quick Commands Reference

### Local Testing with Production Config
```bash
# Set environment variables locally
set SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

### Build Frontend for Production
```bash
cd frontend
npm run build
npm run preview  # Test locally
```

---

## Environment Variables Summary

### Backend (Render)
```env
SPRING_PROFILES_ACTIVE=prod
PORT=8080
DATABASE_URL=jdbc:mysql://host:3306/learnnow
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
YOUTUBE_API_KEY=your-key
GROQ_API_KEY=your-key
JWT_SECRET=your-secret-min-32-characters
CORS_ORIGINS=https://your-frontend.vercel.app
```

### Frontend (Vercel/Netlify)
```env
VITE_API_BASE_URL=https://learnnow-api.onrender.com
```

---

## Troubleshooting

### Backend not starting
- Check Render logs for errors
- Verify all environment variables are set
- Ensure database is accessible

### CORS errors
- Update `CORS_ORIGINS` with exact frontend URL
- Redeploy backend after changing

### Frontend API calls failing
- Verify `VITE_API_BASE_URL` is correct
- Check browser console for errors
- Ensure backend is running

### Database connection issues
- Check DATABASE_URL format
- Verify credentials
- Ensure IP whitelist includes Render's IPs

---

## Free Tier Limits

| Service | Free Tier |
|---------|-----------|
| Render | 750 hours/month, sleeps after 15 min inactivity |
| Vercel | Unlimited static sites, 100GB bandwidth |
| Netlify | 100GB bandwidth, 300 build minutes |
| PlanetScale | 5GB storage, 1 billion row reads |

---

## 🎉 Done!

Your LEARNnow app is now live:
- **Frontend**: `https://learnnow.vercel.app`
- **Backend API**: `https://learnnow-api.onrender.com`
- **Swagger Docs**: `https://learnnow-api.onrender.com/swagger-ui.html`
