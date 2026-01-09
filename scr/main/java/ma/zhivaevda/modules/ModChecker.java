package me.zhivaevda.modules;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Arrays;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import me.zhivaevda.Api;
import me.zhivaevda.utils.*;

public class ModChecker {
    private static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    private static final JSONArray everythingResults = new JSONArray();
    private static final JSONArray modsResults = new JSONArray();
    private static final Set<String> everyPatterns = new HashSet<>();
    private static final List<String> IGNORED_PATH = Arrays.asList(
            "Windows\\", "ProgramData\\", "Recovery\\", "PerfLogs\\", "System Volume Information\\", "AppData\\Local\\Microsoft\\Windows\\", "AppData\\Local\\Packages\\", "AppData\\Roaming\\Microsoft\\Windows\\", "AppData\\LocalLow\\", "$WINDOWS.~BT\\", "$WinREAgent\\",
            "JetBrains\\IntelliJ IDEA", "\\.gradle\\caches\\", "\\.gradle\\wrapper\\", "\\.fabric\\processedMods\\", "\\.gradle\\loom-cache\\",
            "\\libraries\\cpw\\mods\\", "\\libraries\\net\\", "\\libraries\\org\\",
            "\\jre", "\\tlauncher_libraries\\cpw\\mods\\", "\\tlauncher_libraries\\net\\", "\\tlauncher_libraries\\org\\", "\\jdk",

            ".dds", ".anim", ".yml", ".html", ".png", ".ogg", ".vpcf_c", ".py", ".js"
    );

    public static JSONObject getEverything(List<String> everyArray) {
        JSONObject outputJson = new JSONObject();
        try {
            everyPatterns.addAll(everyArray);

            File[] roots = File.listRoots();
            List<Future<?>> futures = new ArrayList<>();
            for (File root : roots) {
                futures.add(executor.submit(() -> searchFiles(root.toPath())));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            outputJson.put("mods", modsResults);
            outputJson.put("every", everythingResults);

            return outputJson;
        } catch (Exception e) {
            outputJson.put("mods", modsResults);
            outputJson.put("every", everythingResults);
        } finally {
            executor.shutdown();
        }
        return outputJson;
    }

    private static void searchFiles(Path rootPath) {
        try {
            Files.walkFileTree(rootPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    check(file, attrs);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    check(dir, attrs);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
        } catch (IOException ignored) {}
    }

    private static void check(Path path, BasicFileAttributes attrs) {
        if (path == null || attrs == null) return;

        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) return;

        String name = fileNamePath.toString().toLowerCase(Locale.ROOT);

        if (name.endsWith(".jar") && !name.contains("$")) {
            try {
                if (attrs.size() <= 15000 * 1024) {
                    scanJarFile(path.toFile());
                }
            } catch (Exception e) {
                // poxui
            }
        }

        String res = Searcher.search(name, everyPatterns);
        if (!res.equals("Не найдено!")) {
            addToEverything(path, attrs, res);
        }

        if (name.endsWith(".bat")) {
            long size = attrs.size();
            if (size <= 15248 && size >= 13824) {
                addToEverything(path, attrs, ".bat size:14kb");
            }
        }
    }

    private static void addToEverything(Path path, BasicFileAttributes attrs, String foundPattern) {
        if (attrs == null) return;

        for (String no : IGNORED_PATH) {
            if (path.toAbsolutePath().toString().contains(no)) {
                return;
            }
        }

        long fileSizeBytes = attrs.size();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModified = sdf.format(new Date(attrs.lastModifiedTime().toMillis()));

        JSONObject fileJson = new JSONObject();
        fileJson.put("name", path.getFileName().toString());
        fileJson.put("detect", foundPattern);
        double sizeKB = (double) fileSizeBytes / 1024;
        fileJson.put("size", Math.round(sizeKB));
        fileJson.put("data", lastModified);
        fileJson.put("path", path.toAbsolutePath().toString().replace("\\", "/"));

        synchronized (everythingResults) {
            everythingResults.put(fileJson);
        }
    }

    private static void scanJarFile(File file) {
        Set<String> importsSet = new HashSet<>();
        Set<String> methodsSet = new HashSet<>();

        List<String> methodsNeFactForge = Arrays.asList(
                "func_226277_ct_", "func_226278_cu_", "func_226281_cx_", "func_174813_aQ", "func_213142_cg"
        );

        List<String> methodsNeFactFabric = Arrays.asList(
                "method_23317", "method_23321"
        );

        List<String> methodsNeFactLaby = Arrays.asList(
                "dci", "cD", "cH"
        );

        List<String> methodsNeFactInoe = Arrays.asList(
                "func_213302_cg", "unimethod", "XZExpand", "YExpand", "affectToAura", "smartCrit", "espLength", "espFactor", "espShaking", "espAmplitude", "rotationPoint", "auraLogic"
        );

        List<String> importsNeFactInoe = Arrays.asList(
                "thunder.hack", "meteordevelopment", "baritone.api", "grimRayTrace", "jdbc:sqlite:methods", "jdbc:sqlite:config:external"
        );

        List<String> methodsFabric = Arrays.asList(
                "Box", "method_5829", "class_238", "method_5857"
        );

        List<String> methodsXray = Collections.singletonList(
                "func_184195_f"
        );

        List<String> methodsInoe = Collections.singletonList(
                "func_174826_a"
        );

        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;

                try (InputStream classStream = jarFile.getInputStream(entry)) {
                    ClassNode mashaNoLoveMe = new ClassNode();
                    ClassReader mashaLove;

                    try {
                        mashaLove = new ClassReader(classStream);
                        mashaLove.accept(mashaNoLoveMe, 0);
                    } catch (Exception e) {
                        continue;
                    }

                    try {
                        if (!mashaNoLoveMe.superName.equals("java/lang/Object")) {
                            importsSet.add(mashaNoLoveMe.superName.replace("/", ".").toLowerCase());
                        }
                    } catch (Exception e) {
                        // poxui
                    }

                    for (String iface : mashaNoLoveMe.interfaces) {
                        importsSet.add(iface.replace("/", ".").toLowerCase());
                    }

                    for (FieldNode field : mashaNoLoveMe.fields) {
                        importsSet.add(field.desc.replace("/", ".").toLowerCase());
                    }

                    for (MethodNode method : mashaNoLoveMe.methods) {
                        importsSet.add(method.desc.replace("/", ".").toLowerCase());
                        for (AbstractInsnNode node : method.instructions) {
                            if (node instanceof MethodInsnNode) {
                                MethodInsnNode methodInsn = (MethodInsnNode) node;
                                importsSet.add(methodInsn.owner.replace("/", ".").toLowerCase());
                            }
                        }
                    }
                    for (MethodNode methodNode : mashaNoLoveMe.methods) {
                        InsnList instructions = methodNode.instructions;
                        for (AbstractInsnNode node : instructions) {
                            if (node instanceof MethodInsnNode) {
                                MethodInsnNode methodInsn = (MethodInsnNode) node;
                                methodsSet.add(methodInsn.name);
                                if (methodsInoe.contains(methodInsn.name)) {
                                    String type = getInfo(importsSet);
                                    if (!type.equals("Не определено")) {
                                        addToMods(file.getAbsolutePath(), "AxisAligned (VOID)", "Forge", true);
                                        return;
                                    }
                                }
                                if (methodsXray.contains(methodInsn.name)) {
                                    String type = getInfo(importsSet);
                                    if (!type.equals("Не определено")) {
                                        addToMods(file.getAbsolutePath(), "Возможно X-Ray", type, true);
                                        return;
                                    }
                                }
                                if (methodInsn.desc.contains("Lnet/minecraft/util/math/AxisAlignedBB;")) {
                                    String type = getInfo(importsSet);
                                    if (methodInsn.desc.contains("D") && !methodInsn.name.contains("<init>")) {
                                        if (!type.equals("Не определено")) {
                                            addToMods(file.getAbsolutePath(), "AxisAligned (DOUBLE)", type, true);
                                            return;
                                        }
                                    }
                                }
                                if (methodsFabric.contains(methodInsn.name)) {
                                    String type = getInfo(importsSet);
                                    if (methodInsn.desc.contains("D") && !methodInsn.name.contains("<init>")) {
                                        if (!type.equals("Не определено")) {
                                            addToMods(file.getAbsolutePath(), "AxisAligned (DOUBLE)", type, true);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Файл jar поврежден");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Внутренняя ошибка...");
        }
        String type = getInfo(importsSet);

        for (String method : methodsSet) {
            if (!type.equals("Не определено")) {
                if (methodsNeFactForge.contains(method)) {
                    addToMods(file.getAbsolutePath(), method, type + " (Forge)", false);
                    return;
                } else if (methodsNeFactFabric.contains(method)) {
                    addToMods(file.getAbsolutePath(), method, type + " (Fabric)", false);
                    return;
                } else if (methodsNeFactLaby.contains(method)) {
                    addToMods(file.getAbsolutePath(), method, type + " (LabyMod)", false);
                    return;
                } else if (methodsNeFactInoe.contains(method)) {
                    addToMods(file.getAbsolutePath(), method, type + " (Other)", false);
                    return;
                }
            }
        }

        for (String tempImport : importsSet) {
            if (!type.equals("Не определено")) {
                for (String importMe : importsNeFactInoe) {
                    if (tempImport.contains(importMe)) {
                        addToMods(file.getAbsolutePath(), tempImport, type, false);
                        return;
                    }
                }
            }
        }
    }

    private static void addToMods(String path, String method, String foundType, boolean techno) {
        JSONObject fileJson = new JSONObject();
        fileJson.put("path", path.replace("\\", "/"));
        fileJson.put("method", method);
        fileJson.put("type", foundType);
        fileJson.put("fact", "" + techno);

        synchronized (modsResults) {
            if (!containsJsonObject(fileJson)) {
                modsResults.put(fileJson);
            }
        }
    }

    private static boolean containsJsonObject(JSONObject obj) {
        for (int i = 0; i < modsResults.length(); i++) {
            if (modsResults.getJSONObject(i).similar(obj)) {
                return true;
            }
        }
        return false;
    }

    private static String getInfo(Set<String> imports) {
        String notFount = "Не определено";

        if (!imports.isEmpty()) {
            for (String imp : imports) {
                if (imp.contains("net.minecraftforge") || imp.contains("net.neoforged")) {
                    return "Forge";
                } else if (imp.contains("fabric")) {
                    return "Fabric";
                } else if (imp.contains("labystudio") || imp.contains("labymod")) {
                    return "LabyMod";
                } else if (imp.contains("minecraft")) {
                    return "Vanilla";
                }
            }
        } else {
            return notFount;
        }
        return notFount;
    }
}