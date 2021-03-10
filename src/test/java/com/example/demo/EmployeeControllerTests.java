package com.example.demo;

import com.example.demo.Employee.Employee;
import com.example.demo.Employee.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EmployeeControllerTests
{
    @Autowired
    MockMvc mvc;

    @Autowired
    EmployeeRepository repo;

    @Transactional
    @Rollback
    @Test
    void testPostandGet() throws Exception
    {
        Employee myEmployee = new Employee();
        myEmployee.setName("bobby");
        myEmployee.setStartDate(new Date());

        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletRequestBuilder postRequest = post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(myEmployee));

        this.mvc.perform(postRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name", is("bobby")));
    }

    @Transactional
    @Rollback
    @Test
    void testSaveTwiceAndGetById() throws Exception
    {
        Employee myEmployee = new Employee();
        myEmployee.setName("bobby");
        myEmployee.setStartDate(new Date());

        Employee myEmployee2 = new Employee();
        myEmployee2.setName("james");
        myEmployee2.setStartDate(new Date());

        repo.save(myEmployee); repo.save(myEmployee2);

        // the first one should be bobby
        MockHttpServletRequestBuilder getRequest = get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(getRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name", is("bobby")));

        // this one should return the not found String
        MockHttpServletRequestBuilder failingGetRequest = get("/employees/1000")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(failingGetRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("This item does not exist"));
    }

    @Transactional
    @Rollback
    @Test
    void testSaveTwiceAndPatchTheNameOfTheFirst() throws Exception
    {
        Employee myEmployee = new Employee();
        myEmployee.setName("bobby");
        myEmployee.setStartDate(new Date());

        Employee myEmployee2 = new Employee();
        myEmployee2.setName("james");
        myEmployee2.setStartDate(new Date());

        repo.save(myEmployee);
        repo.save(myEmployee2);

        Map<String, Object> patches = new HashMap<>();
        patches.put("name", "alberto");

        ObjectMapper objectMapper = new ObjectMapper();

        MockHttpServletRequestBuilder patchRequest = patch("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patches));

        this.mvc.perform(patchRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name", is("alberto")));

        // the first one should be bobby
        MockHttpServletRequestBuilder getRequest = get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(getRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name", is("alberto")));
    }

    @Transactional
    @Rollback
    @Test
    void testSaveTwiceAndDeleteTheFirst() throws Exception
    {
        Employee myEmployee = new Employee();
        myEmployee.setName("bobby");
        myEmployee.setStartDate(new Date());

        Employee myEmployee2 = new Employee();
        myEmployee2.setName("james");
        myEmployee2.setStartDate(new Date());

        repo.save(myEmployee);
        repo.save(myEmployee2);

        ObjectMapper objectMapper = new ObjectMapper();

        MockHttpServletRequestBuilder deleteRequest = delete("/employees/1")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(deleteRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("Employee 1 has been deleted -- 1 remaining"));

        // the first one should be bobby
        MockHttpServletRequestBuilder getRequest = get("/employees")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(getRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].name", is("james")));

        // this one should return the not found String
        MockHttpServletRequestBuilder failingDeleteRequest = delete("/employees/1000")
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(failingDeleteRequest)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("This employee does not exist"));
    }
}
