package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String API_PREFIX_HIT = "/hit";
    private static final String API_PREFIX_STATS = "/stats";

    @Autowired
    public StatsClient(@Value("${STATS_SERVER_URL}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createHit(EndpointHitDto body) {
        return post(API_PREFIX_HIT, body);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, @Nullable List<String> uris,
                                           Boolean unique) {
        String startFormatted = FORMATTER.format(start);
        String endFormatted = FORMATTER.format(end);

        String path = "?start={start}&end={end}&uris={uris}&unique={unique}";
        Map<String, Object> parameters = Map.of(
                "start", startFormatted,
                "end", endFormatted,
                "uris", uris != null ? String.join(",", uris) : "",
                "unique", unique);

        Object response = get(API_PREFIX_STATS + path, parameters).getBody();

        try {
            return Arrays.asList(mapper.readValue(mapper.writeValueAsString(response), ViewStatsDto[].class));
        } catch (Exception e) {
            throw new ClassCastException(e.getMessage());
        }
    }
}
