package com.zorvyn.finance_backend.controller;

import com.zorvyn.finance_backend.service.FinancialRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final FinancialRecordService recordService;

    // overall summary - pass userId param for user specific summary
// example: /api/dashboard/summary
// example: /api/dashboard/summary?userId=1
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(recordService.getDashboardSummary(userId));
    }

    // month wise income and expense breakdown
    @GetMapping("/monthly-trends")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends() {
        return ResponseEntity.ok(recordService.getMonthlyTrends());
    }

    // current week income and expense
    @GetMapping("/weekly-trends")
    public ResponseEntity<Map<String, Object>> getWeeklyTrends() {
        return ResponseEntity.ok(recordService.getWeeklyTrends());
    }
}