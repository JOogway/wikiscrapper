package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.domain.WikiLanguage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** UI strings for English and Polish, keyed by {@code messages.properties} entry names. */
@Component
public class UiMessageCatalog {

    private final Map<WikiLanguage, Map<String, String>> messages;

    public UiMessageCatalog() {
        messages = Map.of(
                WikiLanguage.En, load("messages.properties"),
                WikiLanguage.Pl, load("messages_pl.properties"));
    }

    public Map<String, String> forLanguage(WikiLanguage language) {
        return messages.getOrDefault(language, messages.get(WikiLanguage.En));
    }

    public String get(WikiLanguage language, String key, Object... args) {
        String template = forLanguage(language).get(key);
        if (template == null) {
            return key;
        }
        return args.length == 0 ? template : MessageFormat.format(template, args);
    }

    private static Map<String, String> load(String resource) {
        Properties properties = new Properties();
        try (var reader = new InputStreamReader(
                new ClassPathResource(resource).getInputStream(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load UI messages from " + resource, ex);
        }
        Map<String, String> map = HashMap.newHashMap(properties.size());
        properties.forEach((key, value) -> map.put(key.toString(), value.toString()));
        return Collections.unmodifiableMap(map);
    }
}
