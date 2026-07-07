package com.elsys.safebanking.modules.transaction.service;

import com.elsys.safebanking.modules.transaction.dto.TransferRequestDto;
import com.elsys.safebanking.modules.transaction.dto.TransferResponseDto;

public interface TransferService {

    TransferResponseDto transfer(TransferRequestDto request);
}
