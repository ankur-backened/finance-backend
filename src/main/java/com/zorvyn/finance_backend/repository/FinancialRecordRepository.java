package com.zorvyn.finance_backend.repository;

import com.zorvyn.finance_backend.entity.FinancialRecord;
import com.zorvyn.finance_backend.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // filter by type
    List<FinancialRecord> findByType(TransactionType type);

    // filter by category
    List<FinancialRecord> findByCategory(String category);

    // filter by date range
    List<FinancialRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // filter by user
    List<FinancialRecord> findByUserId(Long userId);

    // total income or total expense
    @Query("SELECT SUM(f.amount) FROM FinancialRecord f WHERE f.type = :type")
    Double sumByType(@Param("type") TransactionType type);

    // category wise totals
    @Query("SELECT f.category, SUM(f.amount) FROM FinancialRecord f GROUP BY f.category")
    List<Object[]> sumByCategory();

    // recent transactions
    @Query("SELECT f FROM FinancialRecord f ORDER BY f.date DESC")
    List<FinancialRecord> findRecentRecords(Pageable pageable);

    // monthly trends - returns month, year, type, total
    @Query("SELECT MONTH(f.date), YEAR(f.date), f.type, SUM(f.amount) " +
            "FROM FinancialRecord f " +
            "GROUP BY YEAR(f.date), MONTH(f.date), f.type " +
            "ORDER BY YEAR(f.date), MONTH(f.date)")
    List<Object[]> getMonthlyTrends();

    // weekly trends - current week only
    @Query("SELECT f.type, SUM(f.amount) FROM FinancialRecord f " +
            "WHERE f.date >= :startOfWeek AND f.date <= :endOfWeek " +
            "GROUP BY f.type")
    List<Object[]> getWeeklyTrends(@Param("startOfWeek") LocalDate startOfWeek,
                                   @Param("endOfWeek") LocalDate endOfWeek);

    // search records by keyword in category or notes
    @Query("SELECT f FROM FinancialRecord f WHERE " +
            "LOWER(f.category) LIKE %:keyword% OR " +
            "LOWER(f.notes) LIKE %:keyword%")
    List<FinancialRecord> searchByKeyword(@Param("keyword") String keyword);
}
