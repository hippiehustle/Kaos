import { Progress } from "@/components/ui/progress";
import { useScanner } from "@/hooks/use-scanner";

export default function ScanningStatus() {
  const { progress, isScanning, currentOperation } = useScanner();

  return (
    <div className="bg-charcoal-800/60 backdrop-blur-sm rounded-xl p-6 border border-charcoal-700">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-100">Scanning Progress</h2>
        <div className="flex items-center space-x-2">
          <div className={`w-2 h-2 rounded-full ${isScanning ? 'bg-matte-cyan-500 animate-pulse' : 'bg-gray-500'}`}></div>
          <span className="text-sm text-gray-400">
            {isScanning ? currentOperation : "Ready to scan"}
          </span>
        </div>
      </div>
      
      <div className="space-y-3">
        <div className="flex justify-between text-sm">
          <span className="text-gray-400">Overall Progress</span>
          <span className="text-gray-300">{progress}%</span>
        </div>
        <Progress value={progress} className="h-2" />
        <div className="flex justify-between text-xs text-gray-500">
          <span>{isScanning ? "Scanning..." : "No active scan"}</span>
          <span>{isScanning ? "~2 min remaining" : ""}</span>
        </div>
      </div>
    </div>
  );
}
