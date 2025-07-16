import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export function useScanner() {
  const [isScanning, setIsScanning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [currentOperation, setCurrentOperation] = useState("Ready to scan");
  const queryClient = useQueryClient();

  const scanMutation = useMutation({
    mutationFn: async () => {
      // Simulate creating a scan session
      const response = await fetch("/api/scan-sessions", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: null }),
      });
      
      if (!response.ok) {
        throw new Error("Failed to start scan");
      }
      
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/stats"] });
      queryClient.invalidateQueries({ queryKey: ["/api/nsfw-results"] });
    },
  });

  const startNewScan = () => {
    if (isScanning) return;
    
    setIsScanning(true);
    setProgress(0);
    setCurrentOperation("Initializing scan...");
    
    // Simulate scanning progress
    const progressSteps = [
      { progress: 10, operation: "Scanning directories..." },
      { progress: 25, operation: "Analyzing images..." },
      { progress: 50, operation: "Processing videos..." },
      { progress: 75, operation: "Checking documents..." },
      { progress: 90, operation: "Finalizing results..." },
      { progress: 100, operation: "Scan complete" },
    ];
    
    let stepIndex = 0;
    const progressInterval = setInterval(() => {
      if (stepIndex < progressSteps.length) {
        const step = progressSteps[stepIndex];
        setProgress(step.progress);
        setCurrentOperation(step.operation);
        stepIndex++;
      } else {
        clearInterval(progressInterval);
        setIsScanning(false);
        setCurrentOperation("Ready to scan");
        scanMutation.mutate();
      }
    }, 1000);
  };

  return {
    isScanning,
    progress,
    currentOperation,
    startNewScan,
  };
}
