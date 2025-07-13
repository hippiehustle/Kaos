import { Button } from "@/components/ui/button";
import { FolderOpen, Download, FileText, Clock } from "lucide-react";

export default function QuickActions() {
  const handleMoveToSecureFolder = () => {
    // TODO: Implement move to secure folder functionality
    console.log("Moving NSFW files to secure folder");
  };

  const handleCreateBackup = () => {
    // TODO: Implement backup functionality
    console.log("Creating backup of all files");
  };

  const handleExportReport = async () => {
    try {
      const response = await fetch("/api/export/report");
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "nsfw-scan-report.json";
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error("Failed to export report:", error);
    }
  };

  const handleScheduleScans = () => {
    // TODO: Implement scheduling functionality
    console.log("Scheduling automatic scans");
  };

  return (
    <div className="bg-charcoal-800/60 backdrop-blur-sm rounded-xl p-6 border border-charcoal-700">
      <h2 className="text-lg font-semibold text-gray-100 mb-4">Quick Actions</h2>
      
      <div className="grid grid-cols-2 gap-3">
        <Button
          onClick={handleMoveToSecureFolder}
          className="bg-red-500/20 hover:bg-red-500/30 text-red-400 font-medium py-3 px-4 rounded-xl transition-all duration-200 flex flex-col items-center space-y-1 border border-red-500/30 h-auto"
        >
          <FolderOpen className="w-5 h-5" />
          <span className="text-xs">Move NSFW</span>
        </Button>
        
        <Button
          onClick={handleCreateBackup}
          className="bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 font-medium py-3 px-4 rounded-xl transition-all duration-200 flex flex-col items-center space-y-1 border border-blue-500/30 h-auto"
        >
          <Download className="w-5 h-5" />
          <span className="text-xs">Backup All</span>
        </Button>
        
        <Button
          onClick={handleExportReport}
          className="bg-green-500/20 hover:bg-green-500/30 text-green-400 font-medium py-3 px-4 rounded-xl transition-all duration-200 flex flex-col items-center space-y-1 border border-green-500/30 h-auto"
        >
          <FileText className="w-5 h-5" />
          <span className="text-xs">Export Report</span>
        </Button>
        
        <Button
          onClick={handleScheduleScans}
          className="bg-purple-500/20 hover:bg-purple-500/30 text-purple-400 font-medium py-3 px-4 rounded-xl transition-all duration-200 flex flex-col items-center space-y-1 border border-purple-500/30 h-auto"
        >
          <Clock className="w-5 h-5" />
          <span className="text-xs">Schedule</span>
        </Button>
      </div>
    </div>
  );
}
