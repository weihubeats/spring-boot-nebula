package com.nebula.dynamic.datasource.sample.controller;

import com.nebula.base.model.NebulaPageRes;
import com.nebula.dynamic.datasource.annotation.NebulaWrite;
import com.nebula.dynamic.datasource.sample.dto.TeacherDTO;
import com.nebula.dynamic.datasource.sample.service.TeacherService;
import com.nebula.dynamic.datasource.sample.vo.TeacherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;


    @GetMapping("/read-list")
    @NebulaWrite
    public NebulaPageRes<TeacherVO> read(TeacherDTO dto) {
        return teacherService.list(dto);
    }
}
