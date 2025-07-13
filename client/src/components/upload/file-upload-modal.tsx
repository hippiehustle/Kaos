import { useState, useRef } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Upload, X, CheckCircle } from "lucide-react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useToast } from "@/hooks/use-toast";

interface FileUploadModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function FileUploadModal({ open, onOpenChange }: FileUploadModalProps) {
  const [files, setFiles] = useState<File[]>([]);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const uploadMutation = useMutation({
    mutationFn: async (files: File[]) => {
      const formData = new FormData();
      files.forEach((file) => {
        formData.append("files", file);
      });

      const response = await fetch("/api/upload", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        throw new Error("Upload failed");
      }

      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/stats"] });
      queryClient.invalidateQueries({ queryKey: ["/api/nsfw-results"] });
      toast({
        title: "Upload successful",
        description: "Files have been analyzed successfully.",
      });
      onOpenChange(false);
      setFiles([]);
      setUploadProgress(0);
    },
    onError: () => {
      toast({
        title: "Upload failed",
        description: "There was an error uploading your files.",
        variant: "destructive",
      });
    },
  });

  const handleFileSelect = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(event.target.files || []);
    setFiles(selectedFiles);
  };

  const handleUpload = async () => {
    if (files.length === 0) return;
    
    setUploading(true);
    setUploadProgress(0);

    // Simulate upload progress
    const progressInterval = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 90) {
          clearInterval(progressInterval);
          return prev;
        }
        return prev + 10;
      });
    }, 200);

    try {
      await uploadMutation.mutateAsync(files);
      setUploadProgress(100);
    } finally {
      clearInterval(progressInterval);
      setUploading(false);
    }
  };

  const handleRemoveFile = (index: number) => {
    setFiles(files.filter((_, i) => i !== index));
  };

  const handleDrop = (event: React.DragEvent) => {
    event.preventDefault();
    const droppedFiles = Array.from(event.dataTransfer.files);
    setFiles(droppedFiles);
  };

  const handleDragOver = (event: React.DragEvent) => {
    event.preventDefault();
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-charcoal-800 border border-charcoal-700 text-gray-100 max-w-sm mx-auto">
        <DialogHeader>
          <DialogTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Upload className="w-5 h-5" />
            <span>Upload Files</span>
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {!uploading ? (
            <>
              <div
                className="border-2 border-dashed border-charcoal-600 rounded-xl p-8 text-center hover:border-matte-cyan-600 transition-colors cursor-pointer"
                onDrop={handleDrop}
                onDragOver={handleDragOver}
                onClick={handleFileSelect}
              >
                <Upload className="w-8 h-8 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-300 mb-2">Drop files here or</p>
                <span className="text-matte-cyan-400 hover:text-matte-cyan-300 transition-colors font-medium">
                  Choose Files
                </span>
              </div>

              <input
                ref={fileInputRef}
                type="file"
                multiple
                accept="image/*,video/*,.pdf,.doc,.docx"
                onChange={handleFileChange}
                className="hidden"
              />

              {files.length > 0 && (
                <div className="space-y-2">
                  <h4 className="text-sm font-medium text-gray-200">Selected Files:</h4>
                  <div className="max-h-32 overflow-y-auto space-y-1">
                    {files.map((file, index) => (
                      <div key={index} className="flex items-center justify-between bg-charcoal-700 rounded p-2">
                        <span className="text-xs text-gray-300 truncate">{file.name}</span>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleRemoveFile(index)}
                          className="p-1 h-auto text-gray-400 hover:text-gray-200"
                        >
                          <X className="w-3 h-3" />
                        </Button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <Button
                onClick={handleUpload}
                disabled={files.length === 0 || uploadMutation.isPending}
                className="w-full bg-matte-cyan-600 hover:bg-matte-cyan-700 text-white font-semibold py-3 px-4 rounded-xl transition-all duration-200"
              >
                {uploadMutation.isPending ? "Uploading..." : "Start Analysis"}
              </Button>
            </>
          ) : (
            <div className="space-y-4">
              <div className="text-center">
                <div className="w-12 h-12 bg-matte-cyan-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  {uploadProgress === 100 ? (
                    <CheckCircle className="w-6 h-6 text-white" />
                  ) : (
                    <Upload className="w-6 h-6 text-white" />
                  )}
                </div>
                <h3 className="text-lg font-semibold text-gray-100">
                  {uploadProgress === 100 ? "Analysis Complete!" : "Analyzing Files..."}
                </h3>
                <p className="text-sm text-gray-400">
                  {uploadProgress === 100 ? "Files have been processed" : "Please wait while we scan your files"}
                </p>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-400">Progress</span>
                  <span className="text-gray-300">{uploadProgress}%</span>
                </div>
                <Progress value={uploadProgress} className="h-2" />
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
