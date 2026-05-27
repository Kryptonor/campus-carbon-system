package com.carbon.service;

import com.carbon.dao.ExchangeRecordRepository;
import com.carbon.dao.ProductRepository;
import com.carbon.dao.UserRepository;
import com.carbon.dto.CreateExchangeRequest;
import com.carbon.entity.ExchangeRecord;
import com.carbon.entity.Product;
import com.carbon.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ExchangeService {
    private final ExchangeRecordRepository exchangeRecordRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ExchangeService(ExchangeRecordRepository exchangeRecordRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.exchangeRecordRepository = exchangeRecordRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ExchangeRecord create(CreateExchangeRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("product not found"));

        long totalPoints = product.getPricePoints() * request.amount();
        if (user.getPointsBalance() < totalPoints) {
            throw new IllegalArgumentException("insufficient points");
        }
        if (product.getStock() < request.amount()) {
            throw new IllegalArgumentException("insufficient stock");
        }

        user.setPointsBalance(user.getPointsBalance() - totalPoints);
        product.setStock(product.getStock() - request.amount());
        userRepository.save(user);
        productRepository.save(product);

        // 基于不可篡改审计链路的兑换记录落库，占位链上交易哈希
        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(user.getId());
        record.setProductId(product.getId());
        record.setAmount(request.amount());
        record.setTotalPoints(totalPoints);
        record.setStatus(0);
        record.setRedeemCode(generateRedeemCode());
        record.setRedeemStatus(0);
        record.setTxHash(null);
        return exchangeRecordRepository.save(record);
    }

    @Transactional
    public ExchangeRecord redeem(String redeemCode) {
        ExchangeRecord record = exchangeRecordRepository.findByRedeemCode(redeemCode)
                .orElseThrow(() -> new IllegalArgumentException("redeem code not found"));
        if (record.getRedeemStatus() != null && record.getRedeemStatus() == 1) {
            throw new IllegalArgumentException("redeem code already used");
        }
        record.setRedeemStatus(1);
        record.setRedeemedAt(LocalDateTime.now());
        return exchangeRecordRepository.save(record);
    }

    public Page<ExchangeRecord> list(Long userId, Pageable pageable) {
        if (userId == null) {
            return exchangeRecordRepository.findAll(pageable);
        }
        return exchangeRecordRepository.findByUserId(userId, pageable);
    }

    private String generateRedeemCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
