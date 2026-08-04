package com.huynhliem.service.impl;

import com.huynhliem.dto.request.VoucherRequest;
import com.huynhliem.dto.response.VoucherResponse;
import com.huynhliem.exception.DuplicateResourceException;
import com.huynhliem.exception.InvalidRequestException;
import com.huynhliem.exception.ResourceNotFoundException;
import com.huynhliem.model.User;
import com.huynhliem.model.Voucher;
import com.huynhliem.repository.UserRepository;
import com.huynhliem.repository.VoucherRepository;
import com.huynhliem.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        verifyAdminAccess();

        Optional<Voucher> existingVoucher = voucherRepository.findByCode(request.getCode());
        if (existingVoucher.isPresent()) {
            throw new DuplicateResourceException("Voucher code already exists: " + request.getCode());
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxUses(request.getMaxUses())
                .expiredAt(request.getExpiredAt())
                .build();

        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToResponse(savedVoucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAllVouchers(Pageable pageable) {
        verifyAdminAccess();
        return voucherRepository.findAll(pageable).map(this::mapToResponse);
    }

    private void verifyAdminAccess() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!"ADMIN".equals(user.getRole().getRoleName())) {
            throw new InvalidRequestException("Access denied. Admin role required.");
        }
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxUses(voucher.getMaxUses())
                .usedCount(voucher.getUsedCount())
                .expiredAt(voucher.getExpiredAt())
                .build();
    }
}
