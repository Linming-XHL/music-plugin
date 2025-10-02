package top.lmxhl.music.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NeteaseMusicAPI {
    private static final String API_URL = "https://api.vkeys.cn/v2/music/netease";
    private static final Gson gson = new Gson();

    /**
     * 搜索网易云音乐
     * @param keyword 搜索关键词
     * @return 歌曲信息，如果未找到则返回null
     * @throws IOException 如果网络请求失败
     */
    public static SongInfo searchMusic(String keyword) throws IOException {
        // 编码搜索关键词
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
        
        // 构建API请求URL
        String urlString = API_URL + "?word=" + encodedKeyword + "&choose=1&quality=2";
        URL url = new URL(urlString);
        
        // 发送请求
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        
        // 检查响应码
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("API请求失败，响应码: " + responseCode);
        }
        
        // 读取响应
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();
        
        // 解析JSON响应
        JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
        
        // 检查API响应状态
        if (jsonResponse.get("code").getAsInt() != 200) {
            throw new IOException("API错误: " + jsonResponse.get("message").getAsString());
        }
        
        // 提取歌曲信息
        JsonObject data = jsonResponse.getAsJsonObject("data");
        return new SongInfo(
                data.get("song").getAsString(),
                data.get("singer").getAsString(),
                data.get("url").getAsString()
        );
    }

    /**
     * 歌曲信息封装类
     */
    public static class SongInfo {
        private final String song;
        private final String singer;
        private final String url;

        public SongInfo(String song, String singer, String url) {
            this.song = song;
            this.singer = singer;
            this.url = url;
        }

        public String getSong() {
            return song;
        }

        public String getSinger() {
            return singer;
        }

        public String getUrl() {
            return url;
        }
    }
}
