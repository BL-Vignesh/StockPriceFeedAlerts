package com.portfolio.stockpricefeed.repository;

import com.portfolio.stockpricefeed.entities.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {
    List<AlertHistory> findByUserIdOrderByTimestampDesc(Long userId);
}
