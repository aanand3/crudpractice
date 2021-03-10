package com.example.demo.Employee;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController
{
    private final EmployeeRepository repo;

    public EmployeeController(EmployeeRepository repo)
    {
        this.repo = repo;
    }

    @GetMapping("")
    public Iterable<Employee> listAll() { return repo.findAll(); }

    @PostMapping("")
    public Employee create(@RequestBody Employee newEmployee) { return repo.save(newEmployee); }

    @GetMapping("/{id}")
    public Object read(@PathVariable Long id)
    {
        return repo.existsById(id) ?
                repo.findById(id) :
                "This item does not exist";
    }

    @PatchMapping("/{id}")
    public Object updateOrPost(@PathVariable Long id,
                               @RequestBody Employee newEmployee)
    {
        if (!repo.existsById(id)) return repo.save(newEmployee);

        Employee currEmployee = repo.findById(id).orElse(null);

        if (newEmployee.getId() != null) currEmployee.setId(newEmployee.getId());
        if (newEmployee.getName() != null) currEmployee.setName(newEmployee.getName());
        if (newEmployee.getStartDate() != null) currEmployee.setStartDate(newEmployee.getStartDate());

        return repo.save(currEmployee);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id)
    {
        if (repo.existsById(id))
        {
            repo.deleteById(id);
            return String.format("Employee %d has been deleted -- %d remaining", id, repo.count());
        }
        else return "This employee does not exist" ;
    }
}
