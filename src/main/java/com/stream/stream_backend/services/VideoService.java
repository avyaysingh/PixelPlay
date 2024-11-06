package com.stream.stream_backend.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.stream.stream_backend.entities.Video;
import com.stream.stream_backend.repositories.VideoRepository;

import jakarta.annotation.PostConstruct;

@Service
public class VideoService {

    Logger logger = LoggerFactory.getLogger(VideoService.class);

    private VideoRepository videoRepository;

    @Value("${files.video}")
    String DIR;

    @Value("${file.video.hsl}")
    String HSL_DIR;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    /*
     * @PostConstruct
     * public void iniit() {
     * File file = new File(DIR);
     * 
     * // File file1 = new File(HSL_DIR);
     * 
     * try {
     * Files.createDirectories(Paths.get(HSL_DIR));
     * } catch (IOException e) {
     * throw new RuntimeException(e);
     * }
     * 
     * // if (!file1.exists()) {
     * // file1.mkdir();
     * // System.out.println("Folder Created");
     * // }
     * 
     * if (!file.exists()) {
     * file.mkdir();
     * System.out.println("Folder created");
     * } else {
     * System.out.println("Folder already created");
     * }
     * }
     */

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(DIR));
            Files.createDirectories(Paths.get(HSL_DIR));
            // System.out.println("Directories are ready.");
            logger.info("Video DIR : " + DIR);
            logger.info("HSL_DIR :" + HSL_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories", e);
        }
    }

    // get by id
    public Video getById(String videoId) {
        logger.info("video id :" + videoId);
        return videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video Not found for the id"));
    }

    // saving the video file
    public Video save(Video video, MultipartFile file) {

        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            // file path
            String cleanFileName = StringUtils.cleanPath(filename);

            // folder handling
            String cleanFolder = StringUtils.cleanPath(DIR);

            // folder path with file name
            Path path = Paths.get(cleanFolder, cleanFileName);

            // copying file to folder
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            // VIDEO META DATA: not coming from client side
            video.setContentType(contentType);
            video.setFilePath(path.toString());

            // saving the video
            Video savedVideo = videoRepository.save(video);

            // test code:
            System.out.println(contentType);
            System.out.println(path);

            // processing the video before saving
            processVideo(savedVideo.getVideoId());

            logger.info("File name: " + filename + "\ncontent type: " + contentType);
            // saving : metadata
            return savedVideo;

        } catch (IOException e) {
            logger.error("Video save error : " + e);
            throw new RuntimeException("Failed to save video file", e);
        }

    }

    // get a video
    public Video get(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found for given id"));
    }

    // get by title
    public Video getByTitle(String title) {
        return videoRepository.findByTitle(title)
                .orElseThrow(() -> new RuntimeException("video not found for given title"));
    }

    // getAll Videos
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    // Video processing
    public String processVideo1(String videoId) {
        Video video = this.get(videoId);
        String filePath = video.getFilePath();
        Path videoPath = Paths.get(filePath);
        Path outputPath = Paths.get(HSL_DIR, videoId);

        try {
            Files.createDirectories(outputPath);

            String ffmpegPath = "ffmpeg"; // Full path if not in PATH
            String ffmpegCmd = String.format(
                    "%s -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 " +
                            "-hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/master.m3u8\"",
                    ffmpegPath, videoPath, outputPath, outputPath);

            System.out.println("Executing command: " + ffmpegCmd);

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.redirectErrorStream(true); // Merge error stream with output stream
            Process process = processBuilder.start();

            // Capture and print output for debugging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // Log output for debugging
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Video processing failed with exit code: " + exitCode);
            }

            return videoId;

        } catch (IOException ex) {
            throw new RuntimeException("Video processing failed due to an I/O error: " + ex.getMessage(), ex);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Video processing interrupted", e);
        }
    }

    // process video: updated
    public String processVideo(String videoId) {
        Video video = this.get(videoId);
        String filePath = video.getFilePath();
        Path videoPath = Paths.get(filePath);
        Path outputPath = Paths.get(HSL_DIR, videoId);

        try {
            // Validate input video file existence
            if (!Files.exists(videoPath)) {
                throw new RuntimeException("Input video file does not exist: " + videoPath);
            }

            // Creating output directory if it doesn't exist
            Files.createDirectories(outputPath);

            // Configurable ffmpeg path
            // String ffmpegPath = "ffmpeg";
            String ffmpegPath = "C:\\ffmpeg\\ffmpeg.exe";
            String ffmpegCmd = String.format(
                    "%s -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 " +
                            "-hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/master.m3u8\"",
                    ffmpegPath, videoPath, outputPath, outputPath);

            // String ffmpegCmd2 = String.format(
            // "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10
            // -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"
            // \"%s/master.m3u8\" ",
            // videoPath, outputPath, outputPath);
            // System.out.println("Executing command: " + ffmpegCmd2);

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.redirectErrorStream(true); // Merge error stream with output stream
            Process process = processBuilder.start();

            // Capture and print output for debugging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // System.out.println("Output: " + line);
                    logger.error("Output: ", line);
                }
                while ((line = errorReader.readLine()) != null) {
                    // System.err.println("Error: " + line);
                    logger.error("Error: ", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Video processing failed with exit code: " + exitCode);
            }

            return videoId;

        } catch (IOException ex) {
            throw new RuntimeException("Video processing failed due to an I/O error: " + ex.getMessage(), ex);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Video processing interrupted", e);
        }
    }

    // // video processing
    // public String processVideo(String videoId) {

    // Video video = this.get(videoId);
    // String filePath = video.getFilePath();

    // // path where to store data:
    // Path videoPath = Paths.get(filePath);

    // // String output360p = HSL_DIR + videoId + "/360p/";
    // // String output720p = HSL_DIR + videoId + "/720p/";
    // // String output1080p = HSL_DIR + videoId + "/1080p/";

    // try {
    // // Files.createDirectories(Paths.get(output360p));
    // // Files.createDirectories(Paths.get(output720p));
    // // Files.createDirectories(Paths.get(output1080p));

    // // ffmpeg command
    // Path outputPath = Paths.get(HSL_DIR, videoId);

    // Files.createDirectories(outputPath);

    // String ffmpegCmd = String.format(
    // "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10
    // -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"
    // \"%s/master.m3u8\" ",
    // videoPath, outputPath, outputPath);

    // // StringBuilder ffmpegCmd = new StringBuilder();
    // // ffmpegCmd.append("ffmpeg -i ")
    // // .append(videoPath.toString())
    // // .append(" -c:v libx264 -c:a aac")
    // // .append(" ")
    // // .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
    // // .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
    // // .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
    // // .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
    // // .append("-master_pl_name
    // // ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
    // // .append("-f hls -hls_time 10 -hls_list_size 0 ")
    // // .append("-hls_segment_filename
    // // \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
    // //
    // .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");

    // System.out.println(ffmpegCmd);
    // // file this command
    // ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c",
    // ffmpegCmd);
    // processBuilder.inheritIO();
    // Process process = processBuilder.start();
    // int exit = process.waitFor();
    // if (exit != 0) {
    // throw new RuntimeException("video processing failed!!");
    // }

    // return videoId;

    // } catch (IOException ex) {
    // throw new RuntimeException("Video processing fail!!");
    // } catch (InterruptedException e) {
    // throw new RuntimeException(e);
    // }

    // }

    // public String processVideo(String videoId) {
    // Video video = this.get(videoId);
    // String filePath = video.getFilePath();

    // // Path where to store data:
    // Path videoPath = Paths.get(filePath);
    // Path outputPath = Paths.get(HSL_DIR, videoId);

    // try {
    // Files.createDirectories(outputPath);

    // // Use Docker to run ffmpeg (Windows command)
    // String ffmpegCmd = String.format(
    // "docker run --rm -v \"%s:/input\" -v \"%s:/output\"
    // jrottenberg/ffmpeg:4.1-alpine -i /input/%s -c:v libx264 -c:a aac -strict -2
    // -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename
    // \"/output/segment_%%3d.ts\" \"/output/master.m3u8\"",
    // outputPath.toAbsolutePath(), outputPath.toAbsolutePath(),
    // videoPath.getFileName().toString());

    // System.out.println("FFmpeg Command: " + ffmpegCmd);

    // // Execute the ffmpeg command inside Docker using cmd for Windows
    // ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c",
    // ffmpegCmd);
    // processBuilder.redirectErrorStream(true); // Combine output and error streams
    // Process process = processBuilder.start();

    // // Read output
    // try (BufferedReader reader = new BufferedReader(new
    // InputStreamReader(process.getInputStream()))) {
    // String line;
    // while ((line = reader.readLine()) != null) {
    // System.out.println(line);
    // }
    // }

    // int exitCode = process.waitFor();
    // if (exitCode != 0) {
    // throw new RuntimeException("video processing failed with exit code: " +
    // exitCode);
    // }

    // return videoId;

    // } catch (IOException ex) {
    // throw new RuntimeException("Video processing failed!! IOException: " +
    // ex.getMessage());
    // } catch (InterruptedException e) {
    // throw new RuntimeException("Video processing failed!! InterruptedException: "
    // + e.getMessage());
    // }
    // }

}
