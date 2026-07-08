package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.TransferRequestDto;
import com.elsys.safebanking.dto.TransferResponseDto;
import com.elsys.safebanking.service.TransferService;
import com.elsys.safebanking.routes.ApiRoutes;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.TRANSACTIONS)
public class TransactionController {

    private final TransferService transferService;

    public TransactionController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(@Valid @RequestBody TransferRequestDto request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }
}