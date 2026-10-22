const { app, BrowserWindow, ipcMain, shell } = require("electron");
const { spawn } = require("node:child_process");
const path = require("node:path");
let win;
function createWindow() {
  win = new BrowserWindow({
    width: 1180, height: 780, minWidth: 820, minHeight: 600,
    title: "JARVIS Desktop",
    backgroundColor: "#071426",
    webPreferences: { preload: path.join(__dirname, "preload.js"), contextIsolation: true, nodeIntegration: false }
  });
  win.loadFile("index.html");
}
app.whenReady().then(() => { createWindow(); app.on("activate", () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); }); });
app.on("window-all-closed", () => { if (process.platform !== "darwin") app.quit(); });

const allowed = {
  "notepad": ["notepad.exe", []],
  "calculator": ["calc.exe", []],
  "paint": ["mspaint.exe", []],
  "file explorer": ["explorer.exe", []],
  "explorer": ["explorer.exe", []],
  "task manager": ["taskmgr.exe", []]
};
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
  for (const [key, value] of Object.entries(allowed)) {
    if (text.includes(key) || (key === "calculator" && text.includes("कैलकुलेटर")) || (key === "notepad" && text.includes("नोटपैड"))) {
      try { spawn(value[0], value[1], { detached: true, stdio: "ignore", windowsHide: false }).unref(); return { ok: true, message: key + " app khol diya." }; }
      catch (e) { return { ok: false, message: key + " nahi khul saka: " + e.message }; }
    }
  }
  if (text.includes("time") || text.includes("samay") || text.includes("समय")) return { ok: true, message: "Abhi samay " + new Date().toLocaleTimeString("en-IN") + " hai." };
  if (text.includes("band") || text.includes("close") || text.includes("shutdown") || text.includes("शटडाउन")) return { ok: false, message: "Safety ke liye main shutdown ya apps band karne ka command abhi execute nahi karta." };
  return { ok: false, message: "Yeh command abhi supported nahi hai. Help mein commands dekho." };
});