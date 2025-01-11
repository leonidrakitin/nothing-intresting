package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Employee;
import ru.sushi.delivery.kds.domain.persist.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee get(Long id) {
        return this.employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found id " + id));
    }
}
