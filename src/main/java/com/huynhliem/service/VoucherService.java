package com.huynhliem.service;

import com.huynhliem.dto.request.VoucherRequest;
import com.huynhliem.dto.response.VoucherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherResponse createVoucher(VoucherRequest request);
    Page<VoucherResponse> getAllVouchers(Pageable pageable);
}
