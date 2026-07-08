package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;

public interface TransferService {
    
    TransferResponse transfer(TransferRequest request);
    
}