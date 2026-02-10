const ADMIN_STORE_KEY = "securescanner-admin";
const ADMIN_UNLOCKED_KEY = "securescanner-admin-unlocked";

export interface AdminSettings {
  premiumUnlocked: boolean;
  sentisightEnabled: boolean;
  bugReportVisible: boolean;
  featureRequestVisible: boolean;
  adminUnlocked: boolean;
}

const DEFAULT_ADMIN_SETTINGS: AdminSettings = {
  premiumUnlocked: false,
  sentisightEnabled: false,
  bugReportVisible: false,
  featureRequestVisible: false,
  adminUnlocked: false,
};

export function loadAdminSettings(): AdminSettings {
  try {
    const saved = localStorage.getItem(ADMIN_STORE_KEY);
    if (saved) return { ...DEFAULT_ADMIN_SETTINGS, ...JSON.parse(saved) };
  } catch {}
  return { ...DEFAULT_ADMIN_SETTINGS };
}

export function saveAdminSettings(settings: AdminSettings) {
  localStorage.setItem(ADMIN_STORE_KEY, JSON.stringify(settings));
}

export function isAdminUnlocked(): boolean {
  return loadAdminSettings().adminUnlocked;
}

export function unlockAdmin() {
  const settings = loadAdminSettings();
  settings.adminUnlocked = true;
  saveAdminSettings(settings);
}

export function lockAdmin() {
  const settings = loadAdminSettings();
  settings.adminUnlocked = false;
  saveAdminSettings(settings);
}
