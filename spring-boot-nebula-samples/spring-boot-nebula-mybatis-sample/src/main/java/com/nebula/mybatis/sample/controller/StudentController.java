package com.nebula.mybatis.sample.controller;

import com.nebula.base.model.NebulaPageRes;
import com.nebula.mybatis.sample.dto.StudentDTO;
import com.nebula.mybatis.sample.service.StudentService;
import com.nebula.mybatis.sample.vo.StudentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2025/1/8 18:12
 * @description:
 */
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    @GetMapping("/list")
    public NebulaPageRes<StudentVO> list(StudentDTO studentDTO) {
        return studentService.list(studentDTO);
    }
}
