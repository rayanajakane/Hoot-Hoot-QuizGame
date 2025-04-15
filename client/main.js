const { app, BrowserWindow } = require('electron');

// REF (to allow HTTP): https://stackoverflow.com/questions/44658269/electron-how-to-allow-insecure-https/50419166
app.commandLine.appendSwitch('ignore-certificate-errors');

let appWindow;

function initWindow() {
    appWindow = new BrowserWindow({
        // fullscreen: true,
        height: 1080,
        width: 1920,
        webPreferences: {
            nodeIntegration: true,
            devTools: false,
        },
    });

    // Electron Build Path
    const path = `file://${__dirname}/dist/client/index.html`;
    appWindow.loadURL(path);

    appWindow.setMenuBarVisibility(false);

    // Initialize the DevTools.
    appWindow.webContents.openDevTools();

    appWindow.on('closed', function () {
        appWindow = null;
    });
}

app.on('ready', initWindow);

// Close when all windows are closed.
app.on('window-all-closed', function () {
    // On macOS specific close process
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('activate', function () {
    if (appWindow === null) {
        initWindow();
    }
});
