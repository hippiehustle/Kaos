import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { 
  Settings as SettingsIcon, 
  Shield, 
  Bell, 
  Database, 
  Download,
  Trash2,
  Info
} from "lucide-react";

export default function Settings() {
  return (
    <div className="max-w-md mx-auto px-4 py-6 space-y-6">
      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Shield className="w-5 h-5" />
            <span>Security Settings</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label className="text-sm font-medium text-gray-200">
                Auto-delete NSFW content
              </Label>
              <p className="text-xs text-gray-400">
                Automatically move detected content to secure folder
              </p>
            </div>
            <Switch />
          </div>
          
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label className="text-sm font-medium text-gray-200">
                Secure backup
              </Label>
              <p className="text-xs text-gray-400">
                Create encrypted backups before deletion
              </p>
            </div>
            <Switch defaultChecked />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label className="text-sm font-medium text-gray-200">
                Deep scan mode
              </Label>
              <p className="text-xs text-gray-400">
                Enhanced detection with higher accuracy
              </p>
            </div>
            <Switch />
          </div>
        </CardContent>
      </Card>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Bell className="w-5 h-5" />
            <span>Notifications</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label className="text-sm font-medium text-gray-200">
                Scan completion alerts
              </Label>
              <p className="text-xs text-gray-400">
                Notify when scans are finished
              </p>
            </div>
            <Switch defaultChecked />
          </div>
          
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label className="text-sm font-medium text-gray-200">
                Detection alerts
              </Label>
              <p className="text-xs text-gray-400">
                Immediate alerts for high-risk content
              </p>
            </div>
            <Switch defaultChecked />
          </div>
        </CardContent>
      </Card>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Database className="w-5 h-5" />
            <span>Data Management</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <Button
            variant="outline"
            className="w-full bg-charcoal-700 hover:bg-charcoal-600 text-gray-200 border-charcoal-600 justify-start"
          >
            <Download className="w-4 h-4 mr-2" />
            Export all data
          </Button>
          
          <Button
            variant="outline"
            className="w-full bg-red-500/20 hover:bg-red-500/30 text-red-400 border-red-500/30 justify-start"
          >
            <Trash2 className="w-4 h-4 mr-2" />
            Clear scan history
          </Button>
        </CardContent>
      </Card>

      <Card className="bg-charcoal-800/60 backdrop-blur-sm border border-charcoal-700">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-gray-100 flex items-center space-x-2">
            <Info className="w-5 h-5" />
            <span>About</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="text-sm space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-400">Version</span>
              <span className="text-gray-200">1.0.0</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Last Updated</span>
              <span className="text-gray-200">Today</span>
            </div>
          </div>
          
          <div className="pt-2 border-t border-charcoal-600">
            <p className="text-xs text-gray-400 text-center">
              SecureScanner helps protect your privacy by detecting and managing inappropriate content across your device.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
