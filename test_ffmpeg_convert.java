import java.io.*;

public class test_ffmpeg_convert {
    public static boolean videoToMp3(String ffmpegPath, String inputVideo, String outputMp3) throws IOException, InterruptedException {
        String[] command = {
            ffmpegPath,
            "-i", inputVideo,
            "-vn",
            "-ar", "44100",
            "-ac", "2",
            "-b:a", "192k",
            "-f", "mp3",
            outputMp3
        };
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        int exitCode = process.waitFor();
        return new File(outputMp3).exists() && exitCode == 0;
    }

    public static void main(String[] args) {
        String ffmpegPath = "ffmpeg-7.1.1-full_build/bin/ffmpeg.exe";
        String inputVideo = "V:/VSCode/SP/VID_20250625_034111.mp4";
        String outputMp3 = "V:/VSCode/YP/VID_20250625_034111.mp3";
        try {
            boolean success = videoToMp3(ffmpegPath, inputVideo, outputMp3);
            if (success) {
                System.out.println("转换成功！");
            } else {
                System.out.println("转换失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发生异常，转换失败！");
        }
    }
} 