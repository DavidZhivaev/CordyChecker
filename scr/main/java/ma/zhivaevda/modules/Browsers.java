package me.zhivaevda.modules;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.json.JSONArray;

public class Browsers {
    private static final String[] REGISTRY_PATHS = {
            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
            "SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths"
    };

    private static final String[] BROWSER_NAMES = {
            "Internet Explorer", "Google Chrome", "Mozilla Firefox", "Microsoft Edge", "Safari",
            "Opera", "Opera GX", "Brave", "Vivaldi", "Tor Browser", "UC Browser", "Maxthon",
            "Yandex", "Chromium", "SlimBrowser", "Waterfox", "Pale Moon", "SeaMonkey",
            "Epic Privacy Browser", "Comodo Dragon", "Avant Browser", "Falkon", "Midori",
            "Lynx", "QuteBrowser", "Konqueror", "IceCat", "SRWare Iron", "Basilisk",
            "Dooble", "NetSurf", "K-Meleon", "Otter Browser", "Flock", "Camino"
    };

    public static JSONArray getInstalledBrowsers() {
        JSONArray installedBrowsers = new JSONArray();
        for (String browser : BROWSER_NAMES) {
            if (isBrowserInstalled(browser)) {
                installedBrowsers.put(browser);
            }
        }
        return installedBrowsers;
    }

    private static boolean isBrowserInstalled(String browserName) {
        for (String path : REGISTRY_PATHS) {
            if (checkRegistry(WinReg.HKEY_LOCAL_MACHINE, path, browserName) ||
                    checkRegistry(WinReg.HKEY_CURRENT_USER, path, browserName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkRegistry(WinReg.HKEY root, String path, String browserName) {
        try {
            if (Advapi32Util.registryKeyExists(root, path)) {
                String[] subKeys = Advapi32Util.registryGetKeys(root, path);
                for (String subKey : subKeys) {
                    String fullPath = path + "\\" + subKey;
                    try {
                        if (Advapi32Util.registryValueExists(root, fullPath, "DisplayName")) {
                            String displayName = Advapi32Util.registryGetStringValue(root, fullPath, "DisplayName");
                            if (displayName.toLowerCase().contains(browserName.toLowerCase())) {
                                return true;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static void main(String[] args) {
        JSONArray installedBrowsers = getInstalledBrowsers();
        System.out.println(installedBrowsers.toString(4));
    }
}