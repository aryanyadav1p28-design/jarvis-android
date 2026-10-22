const { contextBridge, ipcRenderer } = require("electron");
contextBridge.exposeInMainWorld("jarvis", {
  runCommand: (text) => ipcRenderer.invoke("jarvis:command", text)
});