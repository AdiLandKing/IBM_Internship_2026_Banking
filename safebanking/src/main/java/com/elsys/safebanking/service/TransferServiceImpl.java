package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequestDto;
import com.elsys.safebanking.dto.TransferResponseDto;
import org.springframework.stereotype.Service;

@Service
public class TransferServiceImpl implements TransferService {

    @Override
    public TransferResponseDto transfer(TransferRequestDto request) {
        // Placeholder: resolve auth context, validate accounts, calculate FX, persist transaction, and write logs later.
        throw new UnsupportedOperationException("TODO: implement transfer orchestration flow");
    }
}
