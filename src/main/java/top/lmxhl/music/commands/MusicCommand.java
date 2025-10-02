package top.lmxhl.music.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import top.lmxhl.music.MusicPlugin;
import top.lmxhl.music.audio.AudioManager;
import top.lmxhl.music.api.NeteaseMusicAPI;
import top.lmxhl.music.api.NeteaseMusicAPI.SongInfo;

public class MusicCommand implements CommandExecutor {
    private final MusicPlugin plugin;
    private final AudioManager audioManager;

    public MusicCommand(MusicPlugin plugin) {
        this.plugin = plugin;
        this.audioManager = plugin.getAudioManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "请使用 /music help 查看可用命令");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "play":
                handlePlay(sender, args);
                break;
            case "pause":
                handlePause(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "help":
                handleHelp(sender);
                break;
            case "netease":
                handleNetease(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "未知命令，请使用 /music help 查看帮助");
        }

        return true;
    }

    private void handlePlay(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /music play <歌曲URL>");
            return;
        }

        if (audioManager.isPlaying()) {
            sender.sendMessage(ChatColor.RED + "当前已有音乐正在播放，请先使用 /music pause 暂停");
            return;
        }

        String url = args[1];
        sender.sendMessage(ChatColor.GREEN + "正在准备播放音乐...");
        audioManager.play(url, null, null);
    }

    private void handlePause(CommandSender sender) {
        if (audioManager.isPlaying()) {
            audioManager.pause();
            sender.sendMessage(ChatColor.GREEN + "音乐已暂停");
        } else if (audioManager.isPaused()) {
            audioManager.resume();
            sender.sendMessage(ChatColor.GREEN + "音乐已恢复播放");
        } else {
            sender.sendMessage(ChatColor.RED + "当前没有播放中的音乐");
        }
    }

    private void handleInfo(CommandSender sender) {
        if (!audioManager.isPlaying() && !audioManager.isPaused()) {
            sender.sendMessage(ChatColor.RED + "当前没有播放中的音乐");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "===== 音乐信息 =====");
        sender.sendMessage(ChatColor.GOLD + "歌曲名: " + ChatColor.WHITE + 
                (audioManager.getSongName() != null ? audioManager.getSongName() : audioManager.getFileName()));
        sender.sendMessage(ChatColor.GOLD + "艺术家: " + ChatColor.WHITE + 
                (audioManager.getArtist() != null ? audioManager.getArtist() : "未知"));
        sender.sendMessage(ChatColor.GOLD + "状态: " + ChatColor.WHITE + 
                (audioManager.isPlaying() ? "播放中" : "已暂停"));
        sender.sendMessage(ChatColor.GOLD + "进度: " + ChatColor.WHITE + 
                audioManager.getProgress() + " / " + audioManager.getDuration());
    }

    private void handleHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "===== 音乐插件帮助 =====");
        sender.sendMessage(ChatColor.GOLD + "/music play <URL> " + ChatColor.WHITE + "- 播放指定URL的音乐");
        sender.sendMessage(ChatColor.GOLD + "/music pause " + ChatColor.WHITE + "- 暂停或恢复播放音乐");
        sender.sendMessage(ChatColor.GOLD + "/music info " + ChatColor.WHITE + "- 查看当前播放音乐的信息");
        sender.sendMessage(ChatColor.GOLD + "/music help " + ChatColor.WHITE + "- 显示本帮助信息");
        sender.sendMessage(ChatColor.GOLD + "/music netease <搜索词> " + ChatColor.WHITE + "- 搜索并播放网易云音乐");
    }

    private void handleNetease(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /music netease <搜索词>");
            return;
        }

        if (audioManager.isPlaying()) {
            sender.sendMessage(ChatColor.RED + "当前已有音乐正在播放，请先使用 /music pause 暂停");
            return;
        }

        String searchTerm = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        sender.sendMessage(ChatColor.GREEN + "正在搜索网易云音乐: " + searchTerm);

        // 在异步线程中执行API请求，避免阻塞主线程
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SongInfo songInfo = NeteaseMusicAPI.searchMusic(searchTerm);
                if (songInfo != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(ChatColor.GREEN + "找到歌曲: " + songInfo.getSong() + " - " + songInfo.getSinger());
                        sender.sendMessage(ChatColor.GREEN + "正在准备播放...");
                        audioManager.play(songInfo.getUrl(), songInfo.getSong(), songInfo.getSinger());
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(ChatColor.RED + "未找到相关歌曲");
                    });
                }
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(ChatColor.RED + "搜索失败: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }
}
