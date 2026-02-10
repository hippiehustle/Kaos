import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Bug, Send, CheckCircle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

export default function BugReport() {
  const { toast } = useToast();
  const [submitted, setSubmitted] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [severity, setSeverity] = useState("medium");
  const [steps, setSteps] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim() || !description.trim()) {
      toast({ title: "Missing Fields", description: "Please fill in the title and description.", variant: "destructive" });
      return;
    }

    const report = {
      type: "bug",
      title,
      description,
      severity,
      stepsToReproduce: steps,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
    };

    const existing = JSON.parse(localStorage.getItem("securescanner-reports") || "[]");
    existing.push(report);
    localStorage.setItem("securescanner-reports", JSON.stringify(existing));

    setSubmitted(true);
    toast({ title: "Bug Report Submitted", description: "Thank you for helping improve SecureScanner!" });
  };

  if (submitted) {
    return (
      <div className="p-4 max-w-md mx-auto flex flex-col items-center justify-center min-h-[60vh]">
        <CheckCircle className="w-16 h-16 text-green-400 mb-4" />
        <h2 className="text-xl font-bold text-gray-100 mb-2">Report Submitted</h2>
        <p className="text-sm text-gray-400 text-center mb-6">
          Your bug report has been saved. We'll look into it as soon as possible.
        </p>
        <Button
          onClick={() => { setSubmitted(false); setTitle(""); setDescription(""); setSteps(""); setSeverity("medium"); }}
          className="bg-matte-cyan hover:bg-matte-cyan/80 text-white"
        >
          Submit Another Report
        </Button>
      </div>
    );
  }

  return (
    <div className="p-4 space-y-4 max-w-md mx-auto">
      <div className="flex items-center space-x-2 mb-2">
        <Bug className="w-6 h-6 text-orange-400" />
        <h1 className="text-xl font-bold text-gray-100">Bug Report</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
          <CardHeader>
            <CardTitle className="text-base font-semibold text-gray-100">Describe the Issue</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Title</Label>
              <Input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Brief summary of the bug"
                className="bg-charcoal-900/60 border-charcoal-600 text-gray-200"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Severity</Label>
              <Select value={severity} onValueChange={setSeverity}>
                <SelectTrigger className="bg-charcoal-900/60 border-charcoal-600 text-gray-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="low">Low - Minor issue</SelectItem>
                  <SelectItem value="medium">Medium - Affects functionality</SelectItem>
                  <SelectItem value="high">High - Major problem</SelectItem>
                  <SelectItem value="critical">Critical - App crashes or data loss</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Description</Label>
              <Textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="What happened? What did you expect to happen?"
                rows={4}
                className="bg-charcoal-900/60 border-charcoal-600 text-gray-200"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Steps to Reproduce (optional)</Label>
              <Textarea
                value={steps}
                onChange={(e) => setSteps(e.target.value)}
                placeholder="1. Go to...&#10;2. Click on...&#10;3. Observe..."
                rows={3}
                className="bg-charcoal-900/60 border-charcoal-600 text-gray-200"
              />
            </div>
          </CardContent>
        </Card>

        <Button
          type="submit"
          className="w-full bg-matte-cyan hover:bg-matte-cyan/80 text-white"
        >
          <Send className="w-4 h-4 mr-2" />
          Submit Bug Report
        </Button>
      </form>
    </div>
  );
}
