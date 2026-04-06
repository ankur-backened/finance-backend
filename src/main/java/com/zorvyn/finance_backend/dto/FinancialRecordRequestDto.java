package com.zorvyn.finance_backend.dto;

import com.zorvyn.finance_backend.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FinancialRecordRequestDto {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Double amount;

    @NotNull(message = "Type is required - INCOME or EXPENSE")
    private TransactionType type;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    // optional field, no validation needed
    private String notes;

    @NotNull(message = "User ID is required")
    private Long userId;
}