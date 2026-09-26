package ru.anblazhnov.springai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimeTools {

    private static final Logger log = LoggerFactory.getLogger(TimeTools.class);

    @Tool(name = "getCurrentTime", description = "get current time in specified time zone")
    public String getCurrentTime(String timezone) {
        log.info("Getting time in {}", timezone);
        return LocalDateTime.now(ZoneId.of(timezone)).toString();
    }

}
