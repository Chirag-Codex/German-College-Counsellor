package com.example.GermanCollege.controller;

import com.example.GermanCollege.service.ProgramSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SyncController {

    private final ProgramSyncService syncService;

    @PostMapping("/sync-programs")
    public String sync()
            throws Exception {

        syncService.syncPrograms();

        return "Programs synced successfully";
    }
}