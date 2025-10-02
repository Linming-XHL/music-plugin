package top.lmxhl.music;

import org.bukkit.plugin.java.JavaPlugin;
import top.lmxhl.music.commands.MusicCommand;
import top.lmxhl.music.audio.AudioManager;

public class MusicPlugin extends JavaPlugin {
    private static MusicPlugin instance;
    private AudioManager audioManager;

    @Override
    public void onEnable() {
        instance = this;
        // 初始化音频管理器
        audioManager = new AudioManager(this);
        
        // 注册指令执行器
        getCommand("music").setExecutor(new MusicCommand(this));
        
        getLogger().info("音乐插件已启用 - 作者: 临明小狐狸");
    }

    @Override
    public void onDisable() {
        // 停止任何正在播放的音频
        if (audioManager != null) {
            audioManager.stop();
        }
        getLogger().info("音乐插件已禁用");
    }

    public static MusicPlugin getInstance() {
        return instance;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }
}
