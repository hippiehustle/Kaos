import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Lightbulb, Send, CheckCircle } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

export default function FeatureRequest() {
  const { toast } = useToast();
  const [submitted, setSubmitted] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("enhancement");
  const [priority, setPriority] = useState("nice-to-have");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim() || !description.trim()) {
      toast({ title: "Missing Fields", description: "Please fill in the title and description.", variant: "destructive" });
      return;
    }

    const request = {
      type: "feature",
      title,
      description,
      category,
      priority,
      timestamp: new Date().toISOString(),
    };

    const existing = JSON.parse(localStorage.getItem("securescanner-reports") || "[]");
    existing.push(request);
    localStorage.setItem("securescanner-reports", JSON.stringify(existing));

    setSubmitted(true);
    toast({ title: "Feature Request Submitted", description: "Thank you for your suggestion!" });
  };

  if (submitted) {
    return (
      <div className="p-4 max-w-md mx-auto flex flex-col items-center justify-center min-h-[60vh]">
        <CheckCircle className="w-16 h-16 text-green-400 mb-4" />
        <h2 className="text-xl font-bold text-gray-100 mb-2">Request Submitted</h2>
        <p className="text-sm text-gray-400 text-center mb-6">
          Your feature request has been saved. We appreciate your feedback!
        </p>
        <Button
          onClick={() => { setSubmitted(false); setTitle(""); setDescription(""); setCategory("enhancement"); setPriority("nice-to-have"); }}
          className="bg-matte-cyan hover:bg-matte-cyan/80 text-white"
        >
          Submit Another Request
        </Button>
      </div>
    );
  }

  return (
    <div className="p-4 space-y-4 max-w-md mx-auto">
      <div className="flex items-center space-x-2 mb-2">
        <Lightbulb className="w-6 h-6 text-yellow-400" />
        <h1 className="text-xl font-bold text-gray-100">Feature Request</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
          <CardHeader>
            <CardTitle className="text-base font-semibold text-gray-100">Your Idea</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Title</Label>
              <Input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Brief name for your feature idea"
                className="bg-charcoal-900/60 border-charcoal-600 text-gray-200"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Category</Label>
              <Select value={category} onValueChange={setCategory}>
                <SelectTrigger className="bg-charcoal-900/60 border-charcoal-600 text-gray-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="enhancement">Enhancement - Improve existing feature</SelectItem>
                  <SelectItem value="new-feature">New Feature - Something entirely new</SelectItem>
                  <SelectItem value="ui-ux">UI/UX - Visual or usability improvement</SelectItem>
                  <SelectItem value="integration">Integration - Connect with another service</SelectItem>
                  <SelectItem value="performance">Performance - Speed or efficiency</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Priority</Label>
              <Select value={priority} onValueChange={setPriority}>
                <SelectTrigger className="bg-charcoal-900/60 border-charcoal-600 text-gray-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="nice-to-have">Nice to Have</SelectItem>
                  <SelectItem value="important">Important - Would use frequently</SelectItem>
                  <SelectItem value="essential">Essential - Critical for my workflow</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-sm text-gray-200">Description</Label>
              <Textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe the feature you'd like to see. What problem would it solve?"
                rows={5}
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
          Submit Feature Request
        </Button>
      </form>
    </div>
  );
}
