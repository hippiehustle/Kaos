# Model Integration Guide for SecureScanner

## Overview

This guide explains how to integrate your trained NSFW detection model into the SecureScanner application.

## Current Architecture

The application is structured to make model integration straightforward:

### Database Schema for Training Data

The `scan_results` table stores all the data you'll need for training:

```sql
CREATE TABLE scan_results (
  id SERIAL PRIMARY KEY,
  session_id INTEGER NOT NULL,
  filename TEXT NOT NULL,
  filepath TEXT NOT NULL,
  file_type TEXT NOT NULL,      -- 'image', 'video', 'document'
  is_nsfw BOOLEAN NOT NULL,     -- Ground truth label
  confidence REAL NOT NULL,     -- Model confidence (0.0-1.0)
  processed BOOLEAN NOT NULL,
  flag_category TEXT,           -- 'explicit', 'suggestive', 'adult', 'violent', 'disturbing'
  original_path TEXT,
  new_path TEXT,
  action_taken TEXT,           -- 'none', 'moved', 'renamed', 'backed_up', 'deleted'
  created_at TIMESTAMP DEFAULT NOW()
);
```

### Key Integration Points

#### 1. File Processing Pipeline

**Location**: `server/routes.ts` - File upload endpoints

Currently stubbed for model integration:
- Files are uploaded via `/api/upload` (to be implemented)
- Processing logic calls your model
- Results stored in database via storage interface

#### 2. Model Prediction Interface

**Recommended Integration Pattern**:

```typescript
// Add to server/model.ts (create this file)
interface NSFWPrediction {
  isNsfw: boolean;
  confidence: number;
  category?: 'explicit' | 'suggestive' | 'adult' | 'violent' | 'disturbing';
}

export async function analyzeFile(filePath: string, fileType: string): Promise<NSFWPrediction> {
  // Your model integration here
  // Return prediction with confidence score and category
}
```

#### 3. Confidence Threshold System

**Location**: `shared/schema.ts` - ScanSession configuration

Users can set custom confidence thresholds:
- `confidenceThreshold` field (default: 0.7)
- Configurable per scan session
- Used to filter results in UI

#### 4. Category Classification

The app supports 5 NSFW categories:
- `explicit` - Clearly adult/sexual content
- `suggestive` - Suggestive but not explicit
- `adult` - General adult content
- `violent` - Violent imagery
- `disturbing` - Disturbing content

#### 5. Real-time Progress Updates

**Location**: `client/src/pages/scan-landing.tsx`

The scan interface shows:
- Total files processed
- NSFW content found
- Real-time progress updates
- Live preview of flagged content

## Implementation Steps

### 1. Create Model Service

Create `server/model.ts`:

```typescript
import { NSFWPrediction } from './types';

export class NSFWModelService {
  async predictFile(filePath: string, fileType: string): Promise<NSFWPrediction> {
    // Load your trained model
    // Process the file
    // Return prediction
  }
}
```

### 2. Update File Upload Endpoint

Modify `server/routes.ts` to integrate model:

```typescript
import { NSFWModelService } from './model';

const modelService = new NSFWModelService();

app.post('/api/upload', upload.single('file'), async (req, res) => {
  const file = req.file;
  
  // Get prediction from your model
  const prediction = await modelService.predictFile(file.path, file.mimetype);
  
  // Store result in database
  const result = await storage.createScanResult({
    sessionId: req.body.sessionId,
    filename: file.originalname,
    filepath: file.path,
    fileType: getFileType(file.mimetype),
    isNsfw: prediction.isNsfw,
    confidence: prediction.confidence,
    flagCategory: prediction.category,
    processed: true
  });
  
  res.json(result);
});
```

### 3. Batch Processing Support

For large scan operations, implement batch processing:

```typescript
// In your model service
async processBatch(files: string[]): Promise<NSFWPrediction[]> {
  // Process multiple files efficiently
  // Return array of predictions
}
```

## Data Export for Training

To export existing data for model training:

```sql
-- Export all scan results with labels
SELECT 
  filepath,
  file_type,
  is_nsfw,
  confidence,
  flag_category,
  created_at
FROM scan_results
WHERE processed = true;

-- Export flagged content only
SELECT * FROM scan_results 
WHERE is_nsfw = true 
ORDER BY confidence DESC;

-- Export by category
SELECT * FROM scan_results 
WHERE flag_category = 'explicit'
ORDER BY confidence DESC;
```

## Performance Considerations

1. **Async Processing**: Use async/await for model predictions
2. **File Streaming**: Process large files in chunks
3. **Caching**: Cache predictions for duplicate files
4. **Batch Processing**: Process multiple files simultaneously
5. **Progress Updates**: Update scan session progress in real-time

## Testing Your Integration

1. Upload test images through the web interface
2. Verify predictions are stored correctly in database
3. Check confidence scores are in 0.0-1.0 range
4. Validate category assignments
5. Test threshold filtering in UI

## Environment Variables

Add these to your deployment:

```bash
MODEL_PATH=/path/to/your/model
MODEL_BATCH_SIZE=32
MODEL_CONFIDENCE_THRESHOLD=0.7
```

## Model Requirements

Your model should:
- Accept image/video file paths as input
- Return confidence scores between 0.0-1.0
- Support batch processing for performance
- Classify into the 5 defined categories
- Handle various file formats (JPEG, PNG, MP4, etc.)

## API Documentation

Once integrated, your model will be called through:
- File upload API: `POST /api/upload`
- Batch scan API: `POST /api/scan-batch`
- Real-time processing during scans

The UI will automatically display your model's predictions with the confidence scores and categories you return.