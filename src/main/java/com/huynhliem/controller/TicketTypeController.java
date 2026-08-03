package com.huynhliem.controller;

import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.TicketTypeResponse;
import com.huynhliem.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket-type")
@RequiredArgsConstructor
@Tag(name = "Ticket Type", description = "API cho hạng vé")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @Operation(summary = "Danh sách hạng vé", description = "Lấy danh sách các hạng vé và giá vé theo ID của buổi ca nhạc")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(examples = @ExampleObject(value = """
                    {
                      "code": 200,
                      "message": "Ticket types retrieved successfully",
                      "data": [
                        { "id": 1, "name": "VIP", "price": 2000000, "totalQuantity": 100, "remainingQuantity": 100, "concertId": 1 },
                        { "id": 2, "name": "Standard", "price": 500000, "totalQuantity": 500, "remainingQuantity": 500, "concertId": 1 }
                      ],
                      "timestamp": "2026-08-03T15:00:00Z"
                    }""")))
    })
    @GetMapping("/concert/{concertId}")
    public ResponseEntity<BaseResponse<List<TicketTypeResponse>>> getTicketTypesByConcert(@PathVariable Long concertId) {
        List<TicketTypeResponse> data = ticketTypeService.getTicketTypesByConcertId(concertId);
        
        return ResponseEntity.ok(
                BaseResponse.<List<TicketTypeResponse>>builder()
                        .code(200)
                        .message("Ticket types retrieved successfully")
                        .data(data)
                        .build());
    }
}
