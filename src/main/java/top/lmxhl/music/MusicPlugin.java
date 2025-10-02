package top.lmxhl.music;

import org.bukkit.plugin.java.JavaPlugin;
import top.lmxhl.music.audio.AudioManager;
import top.lmxhl.music.commands.MusicCommand;

public class MusicPlugin extends JavaPlugin {
    private AudioManager audioManager;

    @Override
    public void onEnable() {
        // 初始化音频管理器
        this.audioManager = new AudioManager(this);
        
        // 注册指令
        this.getCommand("music").setExecutor(new MusicCommand(this));
        
        // 插件启用提示
        getLogger().info("MusicPlugin 已启用！作者：临明小狐狸 | 官网：https://lmxhl.top");
    }

    @Override
    public void onDisable() {
        // 插件禁用时停止播放音乐（调用正确的stopAudio()方法）
        if (this.audioManager != null) {
            this.audioManager.stopAudio();
        }
        
        getLogger().info("MusicPlugin 已禁用！");
    }

    // 提供AudioManager的获取方法（给Command调用）
    public AudioManager getAudioManager() {
        return this.audioManager;
    }
}
