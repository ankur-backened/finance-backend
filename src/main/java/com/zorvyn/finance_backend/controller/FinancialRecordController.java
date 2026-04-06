package com.zorvyn.finance_backend.controller;

import com.zorvyn.finance_backend.dto.FinancialRecordRequestDto;
import com.zorvyn.finance_backend.dto.FinancialRecordResponseDto;
import com.zorvyn.finance_backend.enums.TransactionType;
import com.zorvyn.finance_backend.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @PostMapping
    public ResponseEntity<FinancialRecordResponseDto> createRecord(
            @Valid @RequestBody FinancialRecordRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.createRecord(dto));
    }

    // pagination supported - default page 0, size 10
    // example: /api/records?page=0&size=10
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(recordService.getAllRecords(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialRecordResponseDto> getRecordById(
            @PathVariable Long id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialRecordResponseDto> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequestDto dto) {
        return ResponseEntity.ok(recordService.updateRecord(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok("Record deleted successfully");
    }

    // search by keyword in category or notes
    // example: /api/records/search?keyword=salary
    @GetMapping("/search")
    public ResponseEntity<List<FinancialRecordResponseDto>> searchRecords(
            @RequestParam String keyword) {
        return ResponseEntity.ok(recordService.searchRecords(keyword));
    }

    // filter by type, category, userId or date range
    // example: /api/records/filter?type=INCOME
    // example: /api/records/filter?category=Rent
    // example: /api/records/filter?userId=1
    // example: /api/records/filter?startDate=2026-01-01&endDate=2026-04-06
    @GetMapping("/filter")
    public ResponseEntity<List<FinancialRecordResponseDto>> filterRecords(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (type != null) {
            return ResponseEntity.ok(recordService.getByType(type));
        }
        if (category != null) {
            return ResponseEntity.ok(recordService.getByCategory(category));
        }
        if (userId != null) {
            return ResponseEntity.ok(recordService.getByUserId(userId));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(recordService.getByDateRange(startDate, endDate));
        }

        // no filter applied - return all records
        return ResponseEntity.ok(recordService.getAllRecords());
    }
}