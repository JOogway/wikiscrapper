package com.ots.wikiscrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point for the WikiScrapper application — fetches and stores Wikipedia summaries. */
@SpringBootApplication
public class WikiscrapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikiscrapperApplication.class, args);
    }

}
