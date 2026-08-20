package com.ots.wikiscrapper.web;

import com.ots.wikiscrapper.domain.SyncStatusDto;
import com.ots.wikiscrapper.service.SyncJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Starts Wikipedia synchronization and reports background-job progress.
 */
@RestController
@RequestMapping("/api/sync")
@Tag(name = "Sync")
public class SyncApiController {

    private final SyncJobService syncJobService;

    public SyncApiController(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @PostMapping
    @Operation(summary = "Start a background Wikipedia sync")
    public ResponseEntity<SyncStatusDto> start() {
        if (!syncJobService.tryStart()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(syncJobService.getStatus());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(syncJobService.getStatus());
    }

    @GetMapping("/status")
    @Operation(summary = "Get the current or last sync status")
    public SyncStatusDto status() {
        return syncJobService.getStatus();
    }
}
