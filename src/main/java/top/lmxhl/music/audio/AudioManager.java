package top.lmxhl.music.audio;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import top.lmxhl.music.MusicPlugin;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class AudioManager {
    private final MusicPlugin plugin;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private String currentUrl;
    private String fileName;
    private String songName;
    private String artist;
    private long totalDuration = 0;
    private long currentPosition = 0;
    private File currentOggFile;
    private Clip audioClip;
    private BukkitRunnable progressUpdater;

    public AudioManager(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(String url, String songName, String artist) {
        // 停止当前播放的音频
        stop();
        
        this.currentUrl = url;
        this.fileName = extractFileName(url);
        this.songName = songName;
        this.artist = artist;
        this.currentPosition = 0;
        this.totalDuration = 0;

        // 在异步线程中处理文件下载和转换
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // 下载音频文件
                    File tempFile = downloadFile(url);
                    
                    // 转换为OGG格式
                    currentOggFile = AudioConverter.convertToOgg(tempFile);
                    tempFile.delete(); // 删除原始文件
                    
                    // 准备播放
                    prepareAndPlayAudio(currentOggFile);
                    
                } catch (Exception e) {
                    Bukkit.getLogger().severe("播放音频时出错: " + e.getMessage());
                    e.printStackTrace();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.broadcastMessage("[音乐插件] " + ChatColor.RED + "播放失败: " + e.getMessage());
                    });
                    stop();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void prepareAndPlayAudio(File oggFile) {
        try {
            // 打开音频文件
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(oggFile);
            AudioFormat format = audioStream.getFormat();
            
            // 计算总时长（毫秒）
            long frames = audioStream.getFrameLength();
            totalDuration = (long)(frames / format.getFrameRate() * 1000);
            
            // 获取数据行信息
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);
            
            // 开始播放
            audioClip.start();
            isPlaying = true;
            isPaused = false;
            
            // 广播播放信息
            Bukkit.getScheduler().runTask(plugin, () -> {
                String playMessage = "[音乐插件] " + ChatColor.GREEN + "开始播放: " + 
                        (songName != null ? songName + " - " + artist : fileName);
                Bukkit.broadcastMessage(playMessage);
            });
            
            // 启动进度更新任务
            startProgressUpdater();
            
        } catch (Exception e) {
            throw new RuntimeException("准备音频时出错: " + e.getMessage(), e);
        }
    }

    private void startProgressUpdater() {
        progressUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                if (audioClip == null || !isPlaying || audioClip.isClosed()) {
                    cancel();
                    return;
                }
                
                // 更新当前播放位置
                currentPosition = (long)(audioClip.getMicrosecondPosition() / 1000);
                
                // 检查是否播放完毕
                if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength() - 100000) {
                    stop();
                    Bukkit.broadcastMessage("[音乐插件] " + ChatColor.GREEN + "播放结束");
                    cancel();
                }
            }
        };
        progressUpdater.runTaskTimer(plugin, 0, 20); // 每20 ticks (1秒)更新一次
    }

    public void pause() {
        if (isPlaying && audioClip != null && !audioClip.isClosed()) {
            audioClip.stop();
            isPlaying = false;
            isPaused = true;
        } else if (isPaused && audioClip != null && !audioClip.isClosed()) {
            audioClip.start();
            isPlaying = true;
            isPaused = false;
        }
    }

    public void resume() {
        pause(); // 暂停和恢复使用相同的方法，只是状态切换
    }

    public void stop() {
        if (progressUpdater != null) {
            progressUpdater.cancel();
            progressUpdater = null;
        }
        
        if (audioClip != null && !audioClip.isClosed()) {
            audioClip.stop();
            audioClip.close();
            audioClip = null;
        }
        
        // 删除临时文件
        if (currentOggFile != null && currentOggFile.exists()) {
            currentOggFile.delete();
            currentOggFile = null;
        }
        
        isPlaying = false;
        isPaused = false;
        currentPosition = 0;
        totalDuration = 0;
    }

    private File downloadFile(String url) throws IOException {
        // 创建临时文件
        String fileExtension = getFileExtension(url);
        File tempFile = File.createTempFile("music_" + UUID.randomUUID().toString(), fileExtension);
        tempFile.deleteOnExit();
        
        // 下载文件
        try (java.io.InputStream in = new URL(url).openStream();
             java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        return tempFile;
    }

    private String extractFileName(String url) {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];
        // 移除URL参数
        if (fileName.contains("?")) {
            fileName = fileName.split("\\?")[0];
        }
        return fileName;
    }

    private String getFileExtension(String url) {
        String fileName = extractFileName(url);
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return ".tmp";
    }

    // 获取格式化的进度时间
    public String getProgress() {
        return formatTime(currentPosition);
    }

    // 获取格式化的总时长
    public String getDuration() {
        return formatTime(totalDuration);
    }

    // 将毫秒转换为分:秒格式
    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Getter方法
    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        return artist;
    }
}
