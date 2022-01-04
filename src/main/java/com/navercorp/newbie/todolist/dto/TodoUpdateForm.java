package com.navercorp.newbie.todolist.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TodoUpdateForm {
    private String title;
    private String description;
    private MultipartFile file;
    private Boolean isDone;
}
