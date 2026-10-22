const { app, BrowserWindow, ipcMain, shell, Tray, Menu, nativeImage } = require("electron");
const { autoUpdater } = require("electron-updater");
const { spawn } = require("node:child_process");
const path = require("node:path");
let win, tray, isQuitting = false;

function sendUpdate(message) {
  if (win && !win.isDestroyed()) win.webContents.send("jarvis:update-status", message);
}
function createWindow() {
  win = new BrowserWindow({
    width: 1180, height: 780, minWidth: 820, minHeight: 600,
    title: "JARVIS Desktop", backgroundColor: "#071426",
    webPreferences: { preload: path.join(__dirname, "preload.js"), contextIsolation: true, nodeIntegration: false }
  });
  win.loadFile("index.html");
  // Keep JARVIS running in the background when the window is closed.
  // Use the tray menu's "Quit JARVIS" to fully exit and install downloaded updates.
  win.on("close", (event) => {
    if (!isQuitting) {
      event.preventDefault();
      win.hide();
      sendUpdate("JARVIS background mein chal raha hai. Band karke update install karne ke liye tray menu se Quit JARVIS chuno.");
    }
  });
}
function createTray() {
  // Use a built-in blank native image to avoid a missing external icon dependency.
  try {
    const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32"><rect x="1" y="1" width="30" height="30" rx="8" fill="#071426" stroke="#75e6ff" stroke-width="2"/><path d="M10 8h12v12q0 5-6 5t-6-5" fill="none" stroke="#75e6ff" stroke-width="2.5" stroke-linecap="round"/><circle cx="16" cy="7" r="2" fill="#8ee9d1"/></svg>';
    const icon = nativeImage.createFromDataURL("data:image/svg+xml;base64," + Buffer.from(svg).toString("base64"));
    tray = new Tray(icon);
    tray.setToolTip("JARVIS Desktop");
    tray.setContextMenu(Menu.buildFromTemplate([
      { label: "Open JARVIS", click: () => { if (!win || win.isDestroyed()) createWindow(); win.show(); win.focus(); } },
      { label: "Check for updates", click: () => checkForUpdates() },
      { type: "separator" },
      { label: "Quit JARVIS", click: () => { isQuitting = true; app.quit(); } }
    ]));
    tray.on("double-click", () => { if (win && !win.isDestroyed()) { win.show(); win.focus(); } });
  } catch (e) { console.error("Tray unavailable", e); }
}
function checkForUpdates() {
  if (!app.isPackaged) { sendUpdate("Update check packaged app mein hi available hai."); return; }
  sendUpdate("JARVIS update check kar raha hai…");
  try { autoUpdater.checkForUpdates(); } catch (e) { sendUpdate("Update check abhi nahi ho saka."); }
}
app.whenReady().then(() => {
  ipcMain.handle("jarvis:command", async (_event, raw) => {
    const text = String(raw || "").trim().toLowerCase();
    if (!text) return { ok: false, message: "Command khaali hai." };
    const sites = [
      { keys: ["youtube", "यूट्यूब"], url: "https://www.youtube.com" },
      { keys: ["google", "गूगल"], url: "https://www.google.com" },
      { keys: ["whatsapp", "व्हाट्सएप"], url: "https://web.whatsapp.com" },
      { keys: ["instagram", "इंस्टाग्राम"], url: "https://www.instagram.com" },
      { keys: ["gmail", "जीमेल"], url: "https://mail.google.com" },
      { keys: ["drive", "ड्राइव", "google drive"], url: "https://drive.google.com" }
    ];
    for (const site of sites) {
      if (site.keys.some(k => text.includes(k)) && /(kholo|open|launch|chalao|खोल|चलाओ|खोलो)/i.test(text)) {
        try { await shell.openExternal(site.url); return { ok: true, message: site.keys[0] + " browser mein khol diya." }; }
        catch (e) { return { ok: false, message: "Browser link nahi khul saka: " + e.message }; }
      }
    }
    const allowed = {
      "notepad": "notepad.exe", "calculator": "calc.exe", "paint": "mspaint.exe",
      "file explorer": "explorer.exe", "explorer": "explorer.exe", "task manager": "taskmgr.exe"
    };
    for (const [key, exe] of Object.entries(allowed)) {
      if (text.includes(key) || (key === "calculator" && text.includes("कैलकुलेटर")) || (key === "notepad" && text.includes("नोटपैड"))) {
        try { spawn(exe, [], { detached: true, stdio: "ignore", windowsHide: false }).unref(); return { ok: true, message: key + " app khol diya." }; }
        catch (e) { return { ok: false, message: key + " nahi khul saka: " + e.message }; }
      }
    }
    if (text.includes("time") || text.includes("samay") || text.includes("समय")) return { ok: true, message: "Abhi samay " + new Date().toLocaleTimeString("en-IN") + " hai." };
    if (text.includes("band") || text.includes("close") || text.includes("shutdown") || text.includes("शटडाउन")) return { ok: false, message: "Safety ke liye shutdown ya apps band karne ka command abhi execute nahi karta." };
    if (text.includes("update") || text.includes("अपडेट")) { checkForUpdates(); return { ok: true, message: "Update check shuru kar diya." }; }
    if (text.includes("help") || text.includes("madad") || text.includes("मदद")) return { ok: true, message: "YouTube, Google, WhatsApp, Gmail, Drive, Notepad, Calculator, Paint, Explorer, time aur update commands available hain." };
    return { ok: false, message: "Yeh command abhi supported nahi hai. Help bolkar supported commands dekho." };
  });
  ipcMain.handle("jarvis:check-updates", () => { checkForUpdates(); return true; });
  ipcMain.handle("jarvis:app-version", () => app.getVersion());
  createWindow();
  createTray();
  if (app.isPackaged) {
    autoUpdater.autoDownload = true;
    autoUpdater.autoInstallOnAppQuit = true;
    autoUpdater.on("checking-for-update", () => sendUpdate("Updates check ho rahe hain…"));
    autoUpdater.on("update-available", info => sendUpdate("Naya update mil gaya (" + info.version + "). Download background mein hoga."));
    autoUpdater.on("update-not-available", () => sendUpdate("JARVIS latest version par hai."));
    autoUpdater.on("download-progress", p => sendUpdate("Update download: " + Math.round(p.percent) + "%"));
    autoUpdater.on("update-downloaded", () => sendUpdate("Update download ho gaya. Update install karne ke liye system tray mein JARVIS icon par right-click karke Quit JARVIS chuno."));
    autoUpdater.on("before-quit-for-update", () => { isQuitting = true; });
    autoUpdater.on("error", err => { console.error("Auto-update error", err); sendUpdate("Update abhi nahi ho saka; internet/release baad mein check karein."); });
    setTimeout(checkForUpdates, 8000);
  }
  app.on("activate", () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on("before-quit", () => { isQuitting = true; });
app.on("window-all-closed", () => { if (process.platform !== "darwin" && !tray) app.quit(); });