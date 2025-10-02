package top.lmxhl.music.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import top.lmxhl.music.audio.AudioManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NeteaseMusicAPI {
    // 网易云API地址
    private static final String NETEASE_API_URL = "https://api.vkeys.cn/v2/music/netease?word=%s&choose=1&quality=2";
    private static final Gson gson = new Gson();

    // 搜索并播放网易云音乐
    public static void searchAndPlay(String keyword, AudioManager audioManager, CommandSender sender) {
        try {
            // 对搜索词进行URL编码（支持中文）
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String apiUrl = String.format(NETEASE_API_URL, encodedKeyword);

            // 发送HTTP请求
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);  // 10秒连接超时
            connection.setReadTimeout(10000);     // 10秒读取超时
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            // 检查响应码（200为成功）
            if (connection.getResponseCode() != 200) {
                sender.sendMessage(ChatColor.RED + "API请求失败！响应码：" + connection.getResponseCode());
                connection.disconnect();
                return;
            }

            // 读取API返回结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            // 解析JSON数据
            JsonObject json = gson.fromJson(response.toString(), JsonObject.class);
            if (json.get("code").getAsInt() != 200) {
                String errorMsg = json.get("message").getAsString();
                sender.sendMessage(ChatColor.RED + "搜索失败：" + errorMsg);
                return;
            }

            // 提取歌曲信息（歌曲名、歌手、音频URL）
            JsonObject data = json.getAsJsonObject("data");
            String songName = data.get("song").getAsString();
            String singer = data.get("singer").getAsString();
            String audioUrl = data.get("url").getAsString();

            // 调用AudioManager播放音乐
            sender.sendMessage(ChatColor.GREEN + "搜索成功！找到歌曲：" + songName + " - " + singer);
            audioManager.playAudio(audioUrl, songName, singer);

        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "网络错误！无法连接到API：" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "解析歌曲信息失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
