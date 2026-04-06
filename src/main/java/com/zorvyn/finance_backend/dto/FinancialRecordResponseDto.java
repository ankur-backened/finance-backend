package com.zorvyn.finance_backend.dto;

import com.zorvyn.finance_backend.enums.TransactionType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FinancialRecordResponseDto {

    private Long id;
    private Double amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
    private Long userId;
    private String userName;
}