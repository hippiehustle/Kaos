# SecureScanner - NSFW Content Detection Application

## Project Overview

SecureScanner is a comprehensive Progressive Web App for detecting and managing NSFW content in images, videos, and documents. The application features a mobile-first design with charcoal gray and matte cyan color scheme.

## Architecture

- **Frontend**: React 18 + TypeScript + Vite
- **Backend**: Node.js + Express.js + TypeScript
- **Database**: PostgreSQL with Drizzle ORM
- **UI**: Radix UI + shadcn/ui + Tailwind CSS
- **State Management**: TanStack Query

## Key Features

- Comprehensive scan customization (folders, file types, confidence thresholds)
- Real-time progress tracking with pause/resume controls
- Intelligent file organization by AI categories
- PWA support for mobile installation
- Database persistence with type-safe operations
- Extensive app personalization options

## File Structure

```
/client/                 # React frontend
  /src/
    /components/         # UI components
    /pages/             # Route pages
    /hooks/             # Custom React hooks
    /lib/               # Utilities and query client
/server/                # Express backend
  index.ts              # Server entry point
  routes.ts             # API routes
  storage.ts            # Database operations
  db.ts                 # Database connection
/shared/                # Shared types and schemas
  schema.ts             # Drizzle database schema
```

## Database Schema

### Tables
1. **users** - User management
2. **scan_sessions** - Scan configuration and progress
3. **scan_results** - Individual file analysis results

### Key Features
- File categorization (explicit, suggestive, adult, violent, disturbing)
- Automatic file organization and renaming
- Confidence scoring and threshold filtering
- Progress tracking and session management

## Training Data Structure

The application stores scan results with these key fields for model training:

- `filename`, `filepath`, `fileType`
- `isNsfw` (boolean label)
- `confidence` (0.0-1.0 score)
- `flagCategory` (explicit, suggestive, adult, violent, disturbing)
- `processed` (processing status)

## Setup Instructions

1. Install dependencies: `npm install`
2. Set up PostgreSQL database
3. Configure DATABASE_URL environment variable
4. Push database schema: `npm run db:push`
5. Start development server: `npm run dev`

## API Endpoints

- `POST /api/scan-sessions` - Create scan session
- `GET /api/scan-sessions/:id` - Get session details
- `POST /api/scan-results` - Store scan results
- `GET /api/nsfw-results` - Get flagged content
- `GET /api/stats` - Get scanning statistics

## Model Integration Points

The application is designed to integrate with your trained model at these key points:

1. **File Processing**: When files are uploaded/scanned
2. **Confidence Scoring**: Model outputs 0.0-1.0 confidence scores
3. **Category Classification**: Model assigns specific NSFW categories
4. **Threshold Filtering**: User-configurable confidence thresholds
5. **Result Storage**: All predictions stored for analysis and improvement

The database schema supports storing model predictions, confidence scores, and user feedback for continuous model improvement.