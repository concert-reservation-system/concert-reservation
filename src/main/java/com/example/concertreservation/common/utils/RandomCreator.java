package com.example.concertreservation.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
public class RandomCreator {
    private List<String> adjectives;
    private List<String> nouns;
    private List<String> years;
    private List<String> locations;
    private List<String> themes;

    private final Random random = new Random();

    public RandomCreator() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new File("src/main/java/com/example/concertreservation/common/utils/title.json"));

        adjectives = objectMapper.readValue(node.get("adjectives").toString(), List.class);
        nouns = objectMapper.readValue(node.get("nouns").toString(), List.class);
        years = objectMapper.readValue(node.get("years").toString(), List.class);
        locations = objectMapper.readValue(node.get("locations").toString(), List.class);
        themes = objectMapper.readValue(node.get("themes").toString(), List.class);
    }

    public String generateTitle() {
        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String noun = nouns.get(random.nextInt(nouns.size()));
        String year = years.get(random.nextInt(years.size()));
        String location = locations.get(random.nextInt(locations.size()));
        String theme = themes.get(random.nextInt(themes.size()));

        return String.format("%s %s - %s %s %s", year, location, adjective, noun, theme);
    }

    public Timestamp generateRandomPastTimestamp() {
        int daysToAdd = random.nextInt(72);
        int hoursToAdd = random.nextInt(24);
        Instant pastInstant = Instant.now()
                .minus(daysToAdd, ChronoUnit.DAYS)
                .minus(hoursToAdd, ChronoUnit.HOURS);

        return Timestamp.from(pastInstant);
    }

    public Timestamp generateRandomFutureTimestamp() {
        int daysToAdd = random.nextInt(731);
        int hoursToAdd = random.nextInt(24);
        Instant futureInstant = Instant.now()
                .plus(daysToAdd, ChronoUnit.DAYS)
                .plus(hoursToAdd, ChronoUnit.HOURS);

        return Timestamp.from(futureInstant);
    }
}
