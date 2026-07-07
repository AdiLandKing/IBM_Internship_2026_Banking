package com.elsys.safebanking.modules.transaction.service.impl;

import com.elsys.safebanking.modules.transaction.dto.TransferRequestDto;
import com.elsys.safebanking.modules.transaction.dto.TransferResponseDto;
import com.elsys.safebanking.modules.transaction.service.TransferService;
import org.springframework.stereotype.Service;

@Service
public class TransferServiceImpl implements TransferService {

    @Override
    public TransferResponseDto transfer(TransferRequestDto request) {
        // Placeholder: resolve auth context, validate accounts, calculate FX, persist transaction, and write logs later.
        throw new UnsupportedOperationException("TODO: implement transfer orchestration flow");
    }
}
