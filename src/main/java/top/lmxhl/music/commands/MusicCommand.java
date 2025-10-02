package top.lmxhl.music.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import top.lmxhl.music.MusicPlugin;
import top.lmxhl.music.audio.AudioManager;
import top.lmxhl.music.api.NeteaseMusicAPI;

public class MusicCommand implements CommandExecutor {
    private final MusicPlugin plugin;
    private final AudioManager audioManager;

    // 构造方法：接收主类实例，获取AudioManager
    public MusicCommand(MusicPlugin plugin) {
        this.plugin = plugin;
        this.audioManager = plugin.getAudioManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 检查指令是否为"music"
        if (!cmd.getName().equalsIgnoreCase("music")) {
            return false;
        }

        // 无参数时显示帮助
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        // 处理子指令
        switch (args[0].toLowerCase()) {
            case "play":
                handlePlayCommand(sender, args);
                break;
            case "pause":
                handlePauseCommand(sender);
                break;
            case "info":
                handleInfoCommand(sender);
                break;
            case "help":
                sendHelpMessage(sender);
                break;
            case "netease":
                handleNeteaseCommand(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "未知子指令！使用 /music help 查看帮助");
                break;
        }
        return true;
    }

    // 处理 /music play <URL>
    private void handlePlayCommand(CommandSender sender, String[] args) {
        // 检查参数是否完整
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法错误！正确格式：/music play <音频URL>");
            return;
        }

        String audioUrl = args[1];
        // 调用AudioManager的playAudio()（标题用URL文件名，艺术家为空）
        String fileName = audioUrl.substring(audioUrl.lastIndexOf("/") + 1);
        audioManager.playAudio(audioUrl, fileName, "");
    }

    // 处理 /music pause（暂停/继续）
    private void handlePauseCommand(CommandSender sender) {
        // 调用togglePause()（统一处理暂停/继续）
        audioManager.togglePause();
    }

    // 处理 /music info（查看播放信息）
    private void handleInfoCommand(CommandSender sender) {
        // 直接调用getInfo()（已包含完整播放信息）
        String info = audioManager.getInfo();
        sender.sendMessage(info);
    }

    // 处理 /music netease <搜索词>（网易云搜索播放）
    private void handleNeteaseCommand(CommandSender sender, String[] args) {
        // 检查参数是否完整
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法错误！正确格式：/music netease <歌曲名/歌手名>");
            return;
        }

        // 拼接搜索词（支持多词搜索）
        StringBuilder searchWord = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            searchWord.append(args[i]).append(" ");
        }
        String keyword = searchWord.toString().trim();

        // 调用网易云API获取歌曲信息并播放
        sender.sendMessage(ChatColor.YELLOW + "正在搜索歌曲：" + keyword + "，请稍候...");
        NeteaseMusicAPI.searchAndPlay(keyword, audioManager, sender);
    }

    // 发送帮助信息
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "===== MusicPlugin 帮助列表 =====");
        sender.sendMessage(ChatColor.WHITE + "/music play <URL> " + ChatColor.GRAY + " - 播放指定URL的音频（支持Ogg/Mp3/Wav）");
        sender.sendMessage(ChatColor.WHITE + "/music pause " + ChatColor.GRAY + " - 暂停/继续当前播放的音乐");
        sender.sendMessage(ChatColor.WHITE + "/music info " + ChatColor.GRAY + " - 查看当前音乐的播放进度和信息");
        sender.sendMessage(ChatColor.WHITE + "/music help " + ChatColor.GRAY + " - 显示此帮助列表");
        sender.sendMessage(ChatColor.WHITE + "/music netease <搜索词> " + ChatColor.GRAY + " - 搜索并播放网易云音乐");
        sender.sendMessage(ChatColor.GREEN + "===============================");
        sender.sendMessage(ChatColor.GRAY + "作者：临明小狐狸 | 官网：https://lmxhl.top");
    }
}
