package com.zorvyn.finance_backend.service;

import com.zorvyn.finance_backend.dto.FinancialRecordRequestDto;
import com.zorvyn.finance_backend.dto.FinancialRecordResponseDto;
import com.zorvyn.finance_backend.entity.FinancialRecord;
import com.zorvyn.finance_backend.entity.User;
import com.zorvyn.finance_backend.enums.TransactionType;
import com.zorvyn.finance_backend.exception.ResourceNotFoundException;
import com.zorvyn.finance_backend.repository.FinancialRecordRepository;
import com.zorvyn.finance_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public FinancialRecordResponseDto createRecord(FinancialRecordRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + dto.getUserId()
                ));

        FinancialRecord record = FinancialRecord.builder()
                .amount(dto.getAmount())
                .type(dto.getType())
                .category(dto.getCategory())
                .date(dto.getDate())
                .notes(dto.getNotes())
                .user(user)
                .build();

        return mapToResponse(recordRepository.save(record));
    }

    // paginated version - used by GET /api/records
    public Map<String, Object> getAllRecords(int page, int size) {
        org.springframework.data.domain.Page<FinancialRecord> recordPage =
                recordRepository.findAll(PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("records", recordPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
        response.put("currentPage", recordPage.getNumber());
        response.put("totalRecords", recordPage.getTotalElements());
        response.put("totalPages", recordPage.getTotalPages());
        response.put("isLastPage", recordPage.isLast());

        return response;
    }

    // non paginated version - used by filter endpoint as fallback
    public List<FinancialRecordResponseDto> getAllRecords() {
        return recordRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FinancialRecordResponseDto getRecordById(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id
                ));
        return mapToResponse(record);
    }

    public FinancialRecordResponseDto updateRecord(Long id, FinancialRecordRequestDto dto) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id
                ));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + dto.getUserId()
                ));

        record.setAmount(dto.getAmount());
        record.setType(dto.getType());
        record.setCategory(dto.getCategory());
        record.setDate(dto.getDate());
        record.setNotes(dto.getNotes());
        record.setUser(user);

        return mapToResponse(recordRepository.save(record));
    }

    public void deleteRecord(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id
                ));
        recordRepository.delete(record);
    }

    // --- FILTERS ---

    public List<FinancialRecordResponseDto> getByType(TransactionType type) {
        return recordRepository.findByType(type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FinancialRecordResponseDto> getByCategory(String category) {
        return recordRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FinancialRecordResponseDto> getByDateRange(LocalDate start, LocalDate end) {
        return recordRepository.findByDateBetween(start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FinancialRecordResponseDto> getByUserId(Long userId) {
        return recordRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // keyword search - searches in category and notes fields
    public List<FinancialRecordResponseDto> searchRecords(String keyword) {
        return recordRepository.searchByKeyword(keyword.toLowerCase())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- DASHBOARD ---

    public Map<String, Object> getDashboardSummary(Long userId) {
        List<FinancialRecord> records;

        // if userId provided, show summary for that user only
        // otherwise show overall summary for all users
        if (userId != null) {
            records = recordRepository.findByUserId(userId);
        } else {
            records = recordRepository.findAll();
        }

        // calculate total income from filtered records
        double totalIncome = records.stream()
                .filter(r -> r.getType() == TransactionType.INCOME)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();

        // calculate total expense from filtered records
        double totalExpense = records.stream()
                .filter(r -> r.getType() == TransactionType.EXPENSE)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();

        // category wise totals
        Map<String, Double> categoryWiseTotals = new HashMap<>();
        for (FinancialRecord record : records) {
            categoryWiseTotals.merge(
                    record.getCategory(),
                    record.getAmount(),
                    Double::sum
            );
        }

        // recent 10 transactions sorted by date descending
        List<FinancialRecordResponseDto> recentTransactions = records.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(10)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // user wise income - shows how much each user earned
        Map<String, Double> userWiseIncome = new HashMap<>();
        for (FinancialRecord record : records) {
            if (record.getType() == TransactionType.INCOME) {
                userWiseIncome.merge(
                        record.getUser().getName(),
                        record.getAmount(),
                        Double::sum
                );
            }
        }

        // user wise expense - shows how much each user spent
        Map<String, Double> userWiseExpense = new HashMap<>();
        for (FinancialRecord record : records) {
            if (record.getType() == TransactionType.EXPENSE) {
                userWiseExpense.merge(
                        record.getUser().getName(),
                        record.getAmount(),
                        Double::sum
                );
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", totalIncome - totalExpense);
        summary.put("categoryWiseTotals", categoryWiseTotals);
        summary.put("recentTransactions", recentTransactions);
        summary.put("userWiseIncome", userWiseIncome);
        summary.put("userWiseExpense", userWiseExpense);

        return summary;
    }

    public List<Map<String, Object>> getMonthlyTrends() {
        List<Object[]> results = recordRepository.getMonthlyTrends();
        List<Map<String, Object>> trends = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("month", row[0]);
            entry.put("year", row[1]);
            entry.put("type", row[2]);
            entry.put("total", row[3]);
            trends.add(entry);
        }

        return trends;
    }

    public Map<String, Object> getWeeklyTrends() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        List<Object[]> results = recordRepository.getWeeklyTrends(startOfWeek, endOfWeek);

        double weeklyIncome = 0.0;
        double weeklyExpense = 0.0;

        for (Object[] row : results) {
            TransactionType type = (TransactionType) row[0];
            Double total = (Double) row[1];
            if (type == TransactionType.INCOME) {
                weeklyIncome = total;
            } else {
                weeklyExpense = total;
            }
        }

        Map<String, Object> weeklyTrends = new HashMap<>();
        weeklyTrends.put("weekStart", startOfWeek);
        weeklyTrends.put("weekEnd", endOfWeek);
        weeklyTrends.put("weeklyIncome", weeklyIncome);
        weeklyTrends.put("weeklyExpense", weeklyExpense);
        weeklyTrends.put("weeklyNetBalance", weeklyIncome - weeklyExpense);

        return weeklyTrends;
    }

    // converts FinancialRecord entity to response dto
    private FinancialRecordResponseDto mapToResponse(FinancialRecord record) {
        FinancialRecordResponseDto dto = new FinancialRecordResponseDto();
        dto.setId(record.getId());
        dto.setAmount(record.getAmount());
        dto.setType(record.getType());
        dto.setCategory(record.getCategory());
        dto.setDate(record.getDate());
        dto.setNotes(record.getNotes());
        dto.setUserId(record.getUser().getId());
        dto.setUserName(record.getUser().getName());
        return dto;
    }
}