package top.lmxhl.music.audio;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import top.lmxhl.music.MusicPlugin;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class AudioManager {
    private final MusicPlugin plugin;
    private Clip audioClip;
    private boolean isPlaying = false;
    private String currentTitle = "";
    private String currentArtist = "";
    private long startTime;

    public AudioManager(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    // 播放音频
    public void playAudio(String url, String title, String artist) {
        if (isPlaying) {
            broadcastMessage(ChatColor.RED + "已有音乐正在播放，请先使用/music pause停止播放");
            return;
        }

        try {
            // 下载并转换音频
            File audioFile = AudioConverter.downloadAndConvert(url);
            if (audioFile == null) {
                broadcastMessage(ChatColor.RED + "音频下载或转换失败");
                return;
            }

            // 加载音频
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            if (audioClip != null && audioClip.isOpen()) {  // 替换isClosed()为isOpen()
                audioClip.close();
            }
            
            audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);
            
            // 播放音频（通过游戏内音效系统广播）
            audioClip.start();
            isPlaying = true;
            currentTitle = title;
            currentArtist = artist;
            startTime = System.currentTimeMillis();
            
            broadcastMessage(ChatColor.GREEN + "开始播放: " + 
                            (artist.isEmpty() ? title : title + " - " + artist));
            
            // 监听播放结束
            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    stopAudio();
                }
            });

        } catch (UnsupportedAudioFileException e) {
            broadcastMessage(ChatColor.RED + "播放失败: 不支持的音频格式");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            broadcastMessage(ChatColor.RED + "播放失败: 音频设备不可用");
            e.printStackTrace();
        } catch (IOException e) {
            broadcastMessage(ChatColor.RED + "播放失败: 文件读取错误");
            e.printStackTrace();
        } catch (Exception e) {
            broadcastMessage(ChatColor.RED + "播放失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 暂停/继续播放
    public void togglePause() {
        if (audioClip == null || !audioClip.isOpen()) {  // 替换isClosed()为!isOpen()
            broadcastMessage(ChatColor.RED + "没有正在播放的音乐");
            return;
        }

        if (isPlaying) {
            audioClip.stop();
            isPlaying = false;
            broadcastMessage(ChatColor.YELLOW + "音乐已暂停");
        } else {
            audioClip.start();
            isPlaying = true;
            broadcastMessage(ChatColor.GREEN + "音乐已继续");
        }
    }

    // 停止播放
    public void stopAudio() {
        if (audioClip != null && audioClip.isOpen()) {  // 替换isClosed()为isOpen()
            audioClip.stop();
            audioClip.close();
        }
        isPlaying = false;
        broadcastMessage(ChatColor.YELLOW + "音乐已停止");
    }

    // 获取播放信息
    public String getInfo() {
        if (audioClip == null || !audioClip.isOpen() || !isPlaying) {  // 修复判断条件
            return ChatColor.RED + "没有正在播放的音乐";
        }

        long currentTime = System.currentTimeMillis() - startTime;
        long totalTime = audioClip.getMicrosecondLength() / 1000;
        
        return ChatColor.GREEN + "当前播放: " + 
               (currentArtist.isEmpty() ? currentTitle : currentTitle + " - " + currentArtist) + "\n" +
               ChatColor.YELLOW + "进度: " + formatTime(currentTime) + 
               "/" + formatTime(totalTime);
    }

    // 格式化时间（毫秒转分:秒）
    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / 1000) / 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // 广播消息给所有玩家
    private void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    // Getter方法
    public boolean isPlaying() {
        return isPlaying;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentArtist() {
        return currentArtist;
    }

    // 内部File类（避免与java.io.File冲突，简化临时文件处理）
    public static class File extends java.io.File {
        public File(String pathname) {
            super(pathname);
        }
    }
}
