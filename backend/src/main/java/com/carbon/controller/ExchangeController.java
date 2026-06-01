package com.carbon.controller;

import com.carbon.dto.CreateExchangeRequest;
import com.carbon.dto.RedeemRequest;
import com.carbon.entity.ExchangeRecord;
import com.carbon.service.ExchangeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {
    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping
    public ResponseEntity<ExchangeRecord> create(@Valid @RequestBody CreateExchangeRequest request) {
        return ResponseEntity.ok(exchangeService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ExchangeRecord>> list(
            @RequestParam(value = "userId", required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(exchangeService.list(userId, pageable));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ExchangeRecord> redeem(@Valid @RequestBody RedeemRequest request) {
        return ResponseEntity.ok(exchangeService.redeem(request.redeemCode()));
    }
}
