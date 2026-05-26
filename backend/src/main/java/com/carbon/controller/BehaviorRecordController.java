package com.carbon.controller;

import com.carbon.entity.BehaviorRecord;
import com.carbon.service.BehaviorRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/behaviors")
public class BehaviorRecordController {
    private final BehaviorRecordService behaviorRecordService;

    public BehaviorRecordController(BehaviorRecordService behaviorRecordService) {
        this.behaviorRecordService = behaviorRecordService;
    }

    @GetMapping
    public ResponseEntity<Page<BehaviorRecord>> list(
            @RequestParam(value = "userId", required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(behaviorRecordService.list(userId, pageable));
    }
}
