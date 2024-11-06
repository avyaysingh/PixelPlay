package com.stream.stream_backend.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stream.stream_backend.app.AppConstants;
import com.stream.stream_backend.dto.CustomMessage;
import com.stream.stream_backend.entities.Video;
import com.stream.stream_backend.services.VideoService;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin("*")
public class VideoController {

    private VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file,
            @RequestParam("title") String title, @RequestParam("description") String description) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());

        Video savedVideo = videoService.save(video, file);

        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder().message("Couldn't upload the video").success(false).build());
        }

    }

    // controller for streaming video
    @GetMapping("/stream/{videoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.get(videoId);

        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Video> getAll() {
        return videoService.getAll();
    }

    // streaming in chunks for mitigating the sever load and resource saving purpose
    @SuppressWarnings("resource")
    @GetMapping("/stream/range/{videoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range) {

        System.out.println(range);

        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();

        // if no rage is specified : send complete file
        if (range == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }

        long rangeStart;
        long rangeEnd;

        String[] ranges = range.replace("bytes=", "").split("-");

        rangeStart = Long.parseLong(ranges[0]);

        // not based on browser, the backend will send chunk size accordingly
        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;

        // if range-end is not provide: take from startRange to complete file length
        // rangeEnd = (ranges.length > 1) ? Long.parseLong(ranges[1]) : fileLength - 1;

        // if (rangeEnd > fileLength - 1) {
        // rangeEnd = fileLength - 1;
        // }

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }

        System.out.println("range start : " + rangeStart);
        System.out.println("range end : " + rangeEnd);
        InputStream inputStream;
        try {

            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;

            HttpHeaders headers = new HttpHeaders();

            byte[] data = new byte[(int) contentLength];
            inputStream.read(data, 0, data.length);

            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");

            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Value("${file.video.hsl}")
    private String HSL_DIR;

    @GetMapping("/{videoId}/master.m3u8")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path path = Paths.get(HSL_DIR, videoId, "master.m3u8");
        System.out.println(path);

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl").body(resource);
    }

    @GetMapping("/{videoId}/{segment}.ts")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Resource> serveSegments(
            @PathVariable String videoId,
            @PathVariable String segment) {

        // create path for segment
        Path path = Paths.get(HSL_DIR, videoId, segment + ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);

    }

}
