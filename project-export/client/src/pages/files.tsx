import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, Video, Image, FileText, Download } from "lucide-react";
import type { ScanResult } from "@shared/schema";

export default function Files() {
  const [filter, setFilter] = useState<"all" | "nsfw" | "safe">("all");

  const { data: nsfwResults = [], isLoading } = useQuery<ScanResult[]>({
    queryKey: ["/api/nsfw-results"],
  });

  const getFileIcon = (fileType: string) => {
    switch (fileType) {
      case "image":
        return <Image className="w-5 h-5" />;
      case "video":
        return <Video className="w-5 h-5" />;
      default:
        return <FileText className="w-5 h-5" />;
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-md mx-auto px-4 py-6">
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="bg-charcoal-800/60 rounded-xl p-4 animate-pulse">
              <div className="h-4 bg-charcoal-600 rounded w-3/4 mb-2"></div>
              <div className="h-3 bg-charcoal-600 rounded w-1/2"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto px-4 py-6 space-y-6">
      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100">
            Detected Files
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex space-x-2">
            <Button
              size="sm"
              variant={filter === "all" ? "default" : "outline"}
              onClick={() => setFilter("all")}
              className="text-xs"
            >
              All
            </Button>
            <Button
              size="sm"
              variant={filter === "nsfw" ? "default" : "outline"}
              onClick={() => setFilter("nsfw")}
              className="text-xs"
            >
              NSFW
            </Button>
            <Button
              size="sm"
              variant={filter === "safe" ? "default" : "outline"}
              onClick={() => setFilter("safe")}
              className="text-xs"
            >
              Safe
            </Button>
          </div>

          {nsfwResults.length === 0 ? (
            <div className="text-center py-8 text-gray-400">
              <AlertTriangle className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>No files detected yet</p>
              <p className="text-sm">Start a scan to analyze your files</p>
            </div>
          ) : (
            <div className="space-y-3">
              {nsfwResults.map((file) => (
                <div
                  key={file.id}
                  className="flex items-center space-x-3 p-3 bg-charcoal-700/50 rounded-lg"
                >
                  <div className="w-10 h-10 bg-red-500/20 rounded-lg flex items-center justify-center text-red-400">
                    {getFileIcon(file.fileType)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium text-gray-200 truncate">
                      {file.filename}
                    </div>
                    <div className="text-xs text-gray-400 truncate">
                      {file.filepath}
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Badge
                      variant={file.isNsfw ? "destructive" : "secondary"}
                      className="text-xs"
                    >
                      {Math.round(file.confidence * 100)}%
                    </Badge>
                    <Button size="sm" variant="ghost" className="p-1">
                      <Download className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
