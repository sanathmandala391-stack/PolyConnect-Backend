package com.polyconnect.repository;

import com.polyconnect.entity.AttendanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceHistoryRepository extends JpaRepository<AttendanceHistory, Long> {
    List<AttendanceHistory> findByStudentPinOrderBySnapshotDateAsc(String studentPin);
    boolean existsByStudentPinAndSnapshotDate(String studentPin, LocalDate snapshotDate);
}
