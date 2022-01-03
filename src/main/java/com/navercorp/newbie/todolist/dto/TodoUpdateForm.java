package com.navercorp.newbie.todolist.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class TodoUpdateForm {
    private String description;
    private MultipartFile multipartFile;
    private Boolean isDone;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;
    private Integer priority;
}
