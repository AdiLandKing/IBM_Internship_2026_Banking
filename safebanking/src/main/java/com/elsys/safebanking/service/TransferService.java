package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequestDto;
import com.elsys.safebanking.dto.TransferResponseDto;

public interface TransferService {

    TransferResponseDto transfer(TransferRequestDto request);
}
