package com.app.expense_splitter.repository;

import com.app.expense_splitter.model.entity.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository <ExpenseGroup, Long> {
    List<ExpenseGroup> findByNameContainingIgnoreCase(String name);
    List<ExpenseGroup> findByMembers_Id(Long userId);
}
