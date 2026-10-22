const { contextBridge, ipcRenderer } = require("electron");
contextBridge.exposeInMainWorld("jarvis", {
  runCommand: (text) => ipcRenderer.invoke("jarvis:command", text),
  checkUpdates: () => ipcRenderer.invoke("jarvis:check-updates"),
  getVersion: () => ipcRenderer.invoke("jarvis:app-version"),
  onUpdateStatus: (callback) => ipcRenderer.on("jarvis:update-status", (_event, message) => callback(message))
});