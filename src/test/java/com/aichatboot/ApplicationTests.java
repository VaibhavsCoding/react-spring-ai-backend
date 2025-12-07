package com.aichatboot;

import com.aichatboot.config.OpenAIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ImportAutoConfiguration(exclude = OpenAIConfig.class)
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
