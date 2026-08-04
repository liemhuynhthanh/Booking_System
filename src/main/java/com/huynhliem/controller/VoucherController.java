package com.huynhliem.controller;

import com.huynhliem.dto.request.VoucherRequest;
import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.VoucherResponse;
import com.huynhliem.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
@Tag(name = "Voucher", description = "API quản lý chiến dịch mã giảm giá (Admin)")
public class VoucherController {

        private final VoucherService voucherService;

        @Operation(summary = "Tạo mới Voucher (Admin)", description = "Tạo chiến dịch mã giảm giá mới. Yêu cầu quyền Admin.")
        @PostMapping("/admin/create")
        public ResponseEntity<BaseResponse<VoucherResponse>> createVoucher(@Valid @RequestBody VoucherRequest request) {
                VoucherResponse response = voucherService.createVoucher(request);
                return ResponseEntity.ok(
                                BaseResponse.<VoucherResponse>builder()
                                                .code(200)
                                                .message("Voucher created successfully")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Danh sách Voucher (Admin)", description = "Lấy danh sách tất cả các mã giảm giá có phân trang. Yêu cầu quyền Admin.")
        @GetMapping("/admin/list")
        public ResponseEntity<BaseResponse<List<VoucherResponse>>> getAllVouchers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<VoucherResponse> responsePage = voucherService.getAllVouchers(pageable);
                return ResponseEntity.ok(
                                BaseResponse.<List<VoucherResponse>>builder()
                                                .code(200)
                                                .message("Get all vouchers successfully")
                                                .data(responsePage.getContent())
                                                .currentPage(responsePage.getNumber())
                                                .pageSize(responsePage.getSize())
                                                .totalItems(responsePage.getTotalElements())
                                                .totalPages(responsePage.getTotalPages())
                                                .build());
        }
}
