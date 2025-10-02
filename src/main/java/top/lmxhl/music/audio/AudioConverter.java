package top.lmxhl.music.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioConverter {
    
    /**
     * 将音频文件转换为OGG格式（Minecraft唱片机支持的格式）
     * @param inputFile 输入音频文件
     * @return 转换后的OGG文件
     * @throws IOException 如果文件操作失败
     * @throws UnsupportedAudioFileException 如果不支持的音频格式
     */
    public static File convertToOgg(File inputFile) throws IOException, UnsupportedAudioFileException {
        // 创建输出文件
        File outputFile = new File(inputFile.getParentFile(), 
                "converted_" + UUID.randomUUID().toString() + ".ogg");
        
        // 读取输入音频
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(inputFile);
        AudioFormat sourceFormat = inputStream.getFormat();
        
        // 定义目标格式 (OGG Vorbis)
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.getSampleRate(),
            16, // 16位
            sourceFormat.getChannels(),
            sourceFormat.getChannels() * 2, // 帧大小
            sourceFormat.getSampleRate(),
            false // 小端字节序
        );
        
        // 转换音频格式
        AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, inputStream);
        
        // 写入OGG文件
        AudioSystem.write(convertedStream, AudioFileFormat.Type.OGG, outputFile);
        
        // 关闭流
        convertedStream.close();
        inputStream.close();
        
        return outputFile;
    }
}
