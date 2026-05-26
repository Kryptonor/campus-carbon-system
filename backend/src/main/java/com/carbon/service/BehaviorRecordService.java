package com.carbon.service;

import com.carbon.dao.BehaviorRecordRepository;
import com.carbon.entity.BehaviorRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BehaviorRecordService {
    private final BehaviorRecordRepository behaviorRecordRepository;

    public BehaviorRecordService(BehaviorRecordRepository behaviorRecordRepository) {
        this.behaviorRecordRepository = behaviorRecordRepository;
    }

    public Page<BehaviorRecord> list(Long userId, Pageable pageable) {
        if (userId == null) {
            return behaviorRecordRepository.findAll(pageable);
        }
        return behaviorRecordRepository.findByUserId(userId, pageable);
    }
}
