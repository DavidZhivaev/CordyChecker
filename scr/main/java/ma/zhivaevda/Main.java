package me.zhivaevda;

import me.zhivaevda.interfaces.*;
import me.zhivaevda.modules.*;
import me.zhivaevda.utils.*;
import org.json.JSONArray;
import javax.swing.*;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.List;

public class Main {
    private static String key;
    private static final JSONObject result = new JSONObject();
    private static Interface inter;

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                inter = new Interface();
                inter.setVisible(true);
            });

            boolean validkey = false;
            while (!validkey) {
                String tempKey = getCopy();
                System.out.println(tempKey);
                key = tempKey;
                if (tempKey.equals("Не найдено!") || tempKey.replace(" ", "").isEmpty() || isNumber(tempKey)) {
                    setValue(0, "Скопируйте код проверки, для начала проверки...");
                } else {
                    boolean codeCheckResponse = Api.miniCheckCode(tempKey);
                    if (!codeCheckResponse) {
                        clean();
                        setValue(0, "Скопированный код не работает! Скопируйте новый...");
                    } else {
                        validkey = true;
                    }
                }

                if (!validkey) {
                    try {
                        Thread.sleep(2500);
                    } catch (Exception e) {
                        // poxui
                    }
                }
            }

            if (Api.checkCode(key).contains("Внутренняя ошибка!") || Api.checkCode(key).contains("Ключ не рабочий!")) {
                inter.setStatus(false);
                inter.setProcent(525);
                inter.setDo("Произошла внутренняя ошибка API...");

                while (true) {
                    try {
                        Thread.sleep(2500);
                    } catch (Exception e) {
                        // poxui
                    }
                }
            }

            inter.setTimer(true);

            setValue(3, "Получаем данные для поиска запрещенного ПО");
            JSONObject data = Api.getData(key);
            if (data.has("status")) {
                System.exit(0);
            } else {
            }

            try {
                JSONObject files = ResourceExtractor.copyFilesToTemp();

                addData(data, files);

                ResourceExtractor.deleteFilesFromTemp();
            } catch (Exception e) {
                inter.setStatus(false);
                inter.setProcent(522);
                e.printStackTrace();
                inter.setDo("Произошла фатальная ошибка, мы отправили ее разработчику!");
                Api.sendError(key, e.getMessage());
            }
            System.out.println(result.toString(4));
        } catch (Exception e) {
            inter.setStatus(false);
            inter.setProcent(523);
            e.printStackTrace();
            inter.setDo("Произошла фатальная ошибка, мы отправили ее разработчику!");
            Api.sendError(key, e.getMessage());
        }
    }

    public static void addData(JSONObject data, JSONObject files) {
        long startTime = System.currentTimeMillis();
        setValue(5, "Устанавливается время начала проверки");

        List<String> normalEvery = Searcher.normalEvery((JSONArray) data.get("every"));
        setValue(55, "Проверяем названия файлов и JAR файлы на хитбоксы");

        try {
            JSONObject everyResult = ModChecker.getEverything(normalEvery);
            if (everyResult.has("mods") && everyResult.has("every")) {
                result.put("mods", everyResult.get("mods"));
                result.put("every", everyResult.get("every"));
            }
        } catch (Exception e) {
            putError("mods", e.getMessage());
            putError("every", e.getMessage());
        }

        try {
            JSONArray dllsResult = DLLChecker.getDlls("javaw");
            result.put("dlls", dllsResult);
        } catch (Exception e) {
            putError("dlls", e.getMessage());
        }

        setValue(56, "Проверяем браузеры компьютера");
        try {
            result.put("browsers", Browsers.getInstalledBrowsers());
        } catch (Exception e) {
            putError("browsers", e.getMessage());
        }
        try {
            result.put("browserDownloads", BrowsersDownloads.getBrowsersDownloads(normalEvery));
        } catch (Exception e) {
            putError("browserDownloads", e.getMessage());
        }
        setValue(58, "Проверяем ранее запущенные EXE файлы");
        JSONObject resultExecuter = Executer.getResult(normalEvery);
        try {
            result.put("exe", resultExecuter.get("exe"));
            result.put("executer", resultExecuter.get("executer"));
        } catch (Exception e) {
            putError("exe", e.getMessage());
            putError("executer", e.getMessage());
        }

        setValue(62, "Проверяем Java Virtual Machine");
        try {
            result.put("JVM", JVMChecker.getResult((JSONArray) data.get("jvm")).get("JVM"));
        } catch (Exception e) {
            putError("JVM", e.getMessage());
        }

        setValue(65, "Проверяем Recent и Prefetch");
        try {
            JSONObject prefetchRecent = RecentPrefetch.getRecentPrefetch((String) files.get("pecmd"), normalEvery);
            if (prefetchRecent.has("prefetch") && prefetchRecent.has("recent")) {
                result.put("prefetch", prefetchRecent.get("prefetch"));
                result.put("recent", prefetchRecent.get("recent"));
                result.put("prefetchMiddle", prefetchRecent.get("prefetchMiddle"));
            }
        } catch (Exception e) {
            putError("prefetch", e.getMessage());
            putError("recent", e.getMessage());
            putError("prefetchMiddle", e.getMessage());
        }

        try {
            JSONObject servises = Servises.getServises();
            result.put("servises", servises);
        } catch (Exception e) {
            putError("servises", e.getMessage());
        }

        setValue(70, "Проверяем журнал логов Windows");
        try {
            JSONArray usnLogs = JournalTrace.getResult((JSONArray) data.get("usn"), (String) files.get("usn"));
            result.put("usn", usnLogs);
        } catch (Exception e) {
            putError("usn", e.getMessage());
        }

        setValue(77, "Получаю общую информацию о системе");
        try {
            JSONObject systeminfo = SystemInfo.getResult();
            result.put("osName", systeminfo.get("osName"));
            result.put("musorReset", systeminfo.get("musorReset"));
            result.put("osStart", systeminfo.get("osStart"));
            result.put("userPriv", systeminfo.get("userPriv"));
            result.put("osInstall", systeminfo.get("osInstall"));
        } catch (Exception e) {
            putError("osName", e.getMessage());
            putError("musorReset", e.getMessage());
            putError("osStart", e.getMessage());
            putError("userPriv", e.getMessage());
            putError("osInstall", e.getMessage());
        }

        setValue(80, "Получаю информацию с Minecraft");
        try {
            JSONObject minecraft = Minecraft.getResult((JSONArray) data.get("logs"));
            result.put("username", minecraft.get("username"));
            result.put("startProcess", minecraft.get("startProcess"));
            result.put("directory", minecraft.get("directory"));
            result.put("server", minecraft.get("server"));
            result.put("logNames", minecraft.get("logNames"));
            result.put("logLines", minecraft.get("logLines"));
            result.put("versions", minecraft.get("versions"));
            result.put("version", minecraft.get("version"));
            result.put("startExplorer", minecraft.get("startExplorer"));
            result.put("crashReports", minecraft.get("crashReports"));
        } catch (Exception e) {
            putError("username", e.getMessage());
            putError("startProcess", e.getMessage());
            putError("directory", e.getMessage());
            putError("server", e.getMessage());
            putError("logNames", e.getMessage());
            putError("logLines", e.getMessage());
            putError("versions", e.getMessage());
            putError("version", e.getMessage());
            putError("startExplorer", e.getMessage());
            putError("crashReports", e.getMessage());
        }

        if (result.get("version").toString().toLowerCase().contains("forge")) {
            try {
                JSONArray asmNetochno = new JSONArray();
                asmNetochno.put("ASM: ");
                JSONArray asmTochno = new JSONArray();
                String asmScanner = (String) files.get("javaw");
                result.put("asm", StringChecker.getStrings(asmNetochno, asmTochno, asmScanner));
            } catch (Exception e) {
                putError("asm", e.getMessage());
            }
        } else {
            JSONObject out = new JSONObject();
            out.put("string", "Not forge version...");
            out.put("detect", "ASM: ");
            result.put("asm", out);
        }

        setValue(85, "Проверяю запущенный процесс JAVAW");
        try {
            JSONArray javawNetochno = (JSONArray) data.get("process");
            JSONArray javawTochno = (JSONArray) data.get("processMain");
            String javawScanner = (String) files.get("javaw");
            result.put("javaw", StringChecker.getStrings(javawNetochno, javawTochno, javawScanner));
        } catch (Exception e) {
            putError("javaw", e.getMessage());
        }

        setValue(90, "Проверяю удаленные файлы - ShellBags");
        try {
            JSONArray shellLogs = ShellBags.getResult((String) files.get("usn"));
            result.put("shellbags", shellLogs);
        } catch (Exception e) {
            putError("shellbags", e.getMessage());
        }

        result.put("hwid", Api.getSystemHWID());

        setValue(96, "Проверяю запущенный процесс EXPLORER");
        try {
            JSONArray explorerNetochno = (JSONArray) data.get("explorer");
            JSONArray explorerTochno = new JSONArray();
            String explorerScanner = (String) files.get("explorer");
            result.put("explorer", StringChecker.getStrings(explorerNetochno, explorerTochno, explorerScanner));
        } catch (Exception e) {
            putError("explorer", e.getMessage());
        }

        setValue(98, "Сохраняю время проверки");
        result.put("timer", getTimer(startTime));
        result.put("code", key);

        setValue(99, "Сохраняю результаты проверки на сайте...");

        JSONObject response = Api.sendResults(result, key);

        inter.setStatus(false);
        inter.setProcent((int) response.get("code"));
        inter.setDo("Результаты отправлены на сайт");
    }

    public static void putError(String error, String key) {
        result.put(key, "error: " + error);
    }

    public static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getTimer(long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        StringBuilder timeString = new StringBuilder();
        if (hours > 0) timeString.append(hours).append(" hour ");
        if (minutes > 0) timeString.append(minutes).append(" min ");
        if (seconds > 0) timeString.append(seconds).append(" sec");
        return timeString.toString().trim();
    }

    public static void setValue(int procent, String doing) {
        inter.setDo(doing);
        inter.setProcent(procent);
    }

    public static String getCopy() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);

            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                    return text;
                } catch (UnsupportedFlavorException | IOException e) {
                    return "Не найдено!";
                }
            }
        } catch (Exception e) {
            return "Не найдено!";
        }
        return "Не найдено!";
    }

    public static void clean() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection emptySelection = new StringSelection("");
            clipboard.setContents(emptySelection, null);
        } catch (Exception e) {
            // poxui
        }
    }
}