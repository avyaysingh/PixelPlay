package com.stream.stream_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.stream.stream_backend.services.VideoService;

@SpringBootTest
class StreamBackendApplicationTests {

	@Autowired
	VideoService videoService;

	@Test
	void contextLoads() {
		videoService.processVideo("53f717c0-d7cb-4c29-b2f6-29cbe145140e");
	}

}
