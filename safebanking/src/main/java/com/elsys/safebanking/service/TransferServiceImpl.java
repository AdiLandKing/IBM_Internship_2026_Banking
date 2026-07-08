package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
import org.springframework.stereotype.Service;

@Service
public class TransferServiceImpl implements TransferService {

    @Override
    public TransferResponse transfer(TransferRequest request) {
        // Placeholder: resolve auth context, validate accounts, calculate FX, persist transaction, and write logs later.
        throw new UnsupportedOperationException("TODO: implement transfer orchestration flow");
    }
    
}