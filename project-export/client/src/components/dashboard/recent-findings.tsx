import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { AlertTriangle, Video, Image, FileText, ChevronRight } from "lucide-react";
import { Link } from "wouter";
import type { ScanResult } from "@shared/schema";

export default function RecentFindings() {
  const { data: nsfwResults = [], isLoading } = useQuery<ScanResult[]>({
    queryKey: ["/api/nsfw-results"],
  });

  const getFileIcon = (fileType: string) => {
    switch (fileType) {
      case "image":
        return <Image className="w-4 h-4" />;
      case "video":
        return <Video className="w-4 h-4" />;
      default:
        return <FileText className="w-4 h-4" />;
    }
  };

  const recentFindings = nsfwResults.slice(0, 3);

  if (isLoading) {
    return (
      <div className="bg-charcoal-800/60 backdrop-blur-sm rounded-xl p-6 border border-charcoal-700">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-100">Recent Findings</h2>
        </div>
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="flex items-center space-x-3 p-3 bg-charcoal-700/50 rounded-lg animate-pulse">
              <div className="w-10 h-10 bg-charcoal-600 rounded-lg"></div>
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-charcoal-600 rounded w-3/4"></div>
                <div className="h-3 bg-charcoal-600 rounded w-1/2"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-charcoal-800/60 backdrop-blur-sm rounded-xl p-6 border border-charcoal-700">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-100">Recent Findings</h2>
        <Link href="/files">
          <Button variant="ghost" size="sm" className="text-matte-cyan-400 hover:text-matte-cyan-300 p-0">
            View All
          </Button>
        </Link>
      </div>
      
      <div className="space-y-3">
        {recentFindings.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            <AlertTriangle className="w-12 h-12 mx-auto mb-4 opacity-50" />
            <p>No recent findings</p>
            <p className="text-sm">Upload files or start a scan to begin detection</p>
          </div>
        ) : (
          recentFindings.map((finding) => (
            <div
              key={finding.id}
              className="flex items-center space-x-3 p-3 bg-charcoal-700/50 rounded-lg"
            >
              <div className="w-10 h-10 bg-red-500/20 rounded-lg flex items-center justify-center text-red-400">
                {getFileIcon(finding.fileType)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-sm font-medium text-gray-200 truncate">
                  {finding.filename}
                </div>
                <div className="text-xs text-gray-400 truncate">
                  {finding.filepath}
                </div>
              </div>
              <div className="flex items-center space-x-2">
                <Badge
                  variant="destructive"
                  className="text-xs bg-red-500/20 text-red-400"
                >
                  {Math.round(finding.confidence * 100)}%
                </Badge>
                <Button variant="ghost" size="sm" className="p-1 text-gray-400 hover:text-gray-200">
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
