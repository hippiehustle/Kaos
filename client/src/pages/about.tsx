import { useState, useRef, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Shield, Cpu, Eye, Lock, Heart } from "lucide-react";
import { useLocation } from "wouter";
import { unlockAdmin, isAdminUnlocked } from "@/lib/admin-store";
import { useToast } from "@/hooks/use-toast";
import kaosForgeImg from "@/assets/images/kaos-forge.png";

const TAP_COUNT_REQUIRED = 7;
const TAP_WINDOW_MS = 3000;

export default function About() {
  const [tapCount, setTapCount] = useState(0);
  const tapTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [, setLocation] = useLocation();
  const { toast } = useToast();

  const handleKaosForgeClick = useCallback(() => {
    if (isAdminUnlocked()) {
      setLocation("/admin");
      return;
    }

    setTapCount((prev) => {
      const newCount = prev + 1;

      if (tapTimerRef.current) clearTimeout(tapTimerRef.current);

      if (newCount >= TAP_COUNT_REQUIRED) {
        unlockAdmin();
        toast({
          title: "Admin Mode Unlocked",
          description: "You now have access to the admin panel.",
        });
        setTimeout(() => setLocation("/admin"), 500);
        return 0;
      }

      tapTimerRef.current = setTimeout(() => setTapCount(0), TAP_WINDOW_MS);
      return newCount;
    });
  }, [setLocation, toast]);

  return (
    <div className="p-4 space-y-4 max-w-md mx-auto">
      <div className="text-center py-4">
        <div className="w-16 h-16 bg-matte-cyan/20 rounded-2xl flex items-center justify-center mx-auto mb-3">
          <Shield className="w-8 h-8 text-matte-cyan" />
        </div>
        <h1 className="text-2xl font-bold text-gray-100">SecureScanner</h1>
        <p className="text-sm text-gray-400 mt-1">Version 1.0.0</p>
      </div>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Eye className="w-5 h-5 text-matte-cyan" />
            <span>What We Do</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <p className="text-sm text-gray-300 leading-relaxed">
            SecureScanner is a privacy-focused content detection application that helps you identify and manage inappropriate content across your files. Using advanced AI models, we scan images and videos to automatically detect and organize NSFW content.
          </p>
        </CardContent>
      </Card>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Cpu className="w-5 h-5 text-matte-cyan" />
            <span>Technology</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="text-sm space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-400">Detection Model</span>
              <span className="text-gray-200">InceptionV3</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Accuracy</span>
              <span className="text-gray-200">~93%</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Analysis Method</span>
              <span className="text-gray-200">Multi-Crop + Weighted</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Cloud API</span>
              <span className="text-gray-200">SentiSight.ai</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Platform</span>
              <span className="text-gray-200">PWA + Android</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Lock className="w-5 h-5 text-matte-cyan" />
            <span>Privacy</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-300 leading-relaxed">
            Your files are processed locally on the server and never shared with third parties unless you explicitly enable cloud-based detection. All scan results are stored securely and can be exported or deleted at any time.
          </p>
        </CardContent>
      </Card>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Heart className="w-5 h-5 text-matte-cyan" />
            <span>Credits</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="text-sm space-y-1">
            <p className="text-gray-300">Built with care using React, TensorFlow.js, and modern web technologies.</p>
            <p className="text-gray-400 text-xs mt-2">NSFWJS by Infinite Red | TensorFlow.js by Google | SentiSight.ai</p>
          </div>
        </CardContent>
      </Card>

      <div className="pt-6 pb-8 flex flex-col items-center">
        <button
          onClick={handleKaosForgeClick}
          className="focus:outline-none active:opacity-70 transition-opacity select-none"
          aria-label="Kaos Forge"
        >
          <img
            src={kaosForgeImg}
            alt="Kaos Forge"
            className="w-24 h-24 rounded-xl opacity-60 hover:opacity-80 transition-opacity"
            draggable={false}
          />
        </button>
        <p className="text-xs text-gray-600 mt-2">Kaos Forge</p>
      </div>
    </div>
  );
}
