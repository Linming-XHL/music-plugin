package top.lmxhl.music.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AudioConverter {
    // 手动定义OGG音频类型（Java Sound API不自带）
    private static final AudioFileFormat.Type OGG_TYPE = new AudioFileFormat.Type("OGG", "ogg");

    // 下载并转换音频为OGG格式
    public static File downloadAndConvert(String url) {
        try {
            // 创建临时文件
            File tempInput = File.createTempFile("audio_input", getFileExtension(url));
            File tempOutput = File.createTempFile("audio_output", ".ogg");
            
            // 下载音频文件
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, tempInput.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 转换为OGG格式
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(tempInput);
            
            // 如果已是OGG格式则直接复制
            if (audioStream.getFormat().toString().contains("OGG") || 
                url.toLowerCase().endsWith(".ogg")) {
                Files.copy(tempInput.toPath(), tempOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                // 转换为OGG（使用PCM编码）
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100,  // 采样率
                    16,     // 位深度
                    2,      // 声道数
                    4,      // 帧大小
                    44100,  // 帧速率
                    false   // 字节顺序
                );
                
                AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                AudioSystem.write(convertedStream, OGG_TYPE, tempOutput);  // 使用手动定义的OGG类型
            }

            // 清理临时文件
            tempInput.deleteOnExit();
            return tempOutput;

        } catch (UnsupportedAudioFileException e) {
            System.err.println("不支持的音频格式: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("文件操作失败: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("音频设备不可用: " + e.getMessage());
        }
        
        return null;
    }

    // 获取文件扩展名
    private static String getFileExtension(String url) {
        if (url.contains(".")) {
            return url.substring(url.lastIndexOf("."));
        }
        return ".tmp";
    }
}
