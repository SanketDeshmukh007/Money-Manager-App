package com.sanket.controller;

import com.sanket.dto.ExpenseDTO;
import com.sanket.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO createdExpense = expenseService.addExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpensesForCurrentMonth() {
        List<ExpenseDTO> expenses = expenseService.getExpensesForCurrentMonth();
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable Long expenseId) {
        expenseService.deleteExpenseById(expenseId);
        return ResponseEntity.noContent().build();
    }
}
