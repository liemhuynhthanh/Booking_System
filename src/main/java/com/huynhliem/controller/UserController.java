package com.huynhliem.controller;

import com.huynhliem.dto.request.UserCreationRequest;
import com.huynhliem.dto.response.BaseResponse;
import com.huynhliem.dto.response.UserResponse;
import com.huynhliem.service.UserService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("list")
    ResponseEntity<?> getAllUser(@RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size){
        List<UserResponse> userResponses=userService.findAll();
    return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.<List<UserResponse>>builder()
            .code(HttpStatus.OK.value())
            .message("Users retrieved successfully")
            .data(userResponses)
            .build());

    }
    @GetMapping("/{id}")
    ResponseEntity<?> getUserById(@PathVariable @Min(1) Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User retrieved successfully")
                .data(userResponse)
                .build());
    }
    @PostMapping()
    ResponseEntity<?> createUser(@RequestBody UserCreationRequest req){
        UserResponse created = userService.save(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("User created successfully")
                .data(created)
                .build());
    }
}
