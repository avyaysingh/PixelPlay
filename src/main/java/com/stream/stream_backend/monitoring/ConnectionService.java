package com.stream.stream_backend.monitoring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stream.stream_backend.entities.Video;
import com.stream.stream_backend.repositories.VideoRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

@Service
public class ConnectionService {

    private Counter videoEndpointCounter = null;

    public ConnectionService(CompositeMeterRegistry meterRegistry) {
        videoEndpointCounter = meterRegistry.counter("video.endpoint.counter");
    }

    Logger logger = LoggerFactory.getLogger(ConnectionService.class);

    @Autowired
    private VideoRepository videoRepository;

    public List<Video> getVideos() {
        logger.trace("Starting Video service: level = TRACE");
        logger.info("Starting Video service: level = INFO");

        videoEndpointCounter.increment();
        return videoRepository.findAll();
    }

    public Video getVideosById(String videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Not Found"));
    }

    public Video addVideo(Video video) {
        return videoRepository.save(video);
    }

}
