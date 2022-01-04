package com.navercorp.newbie.todolist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoGetDetailResponseDto {
    private Long id;
    private String title;
    private String description;
    private String uploadFileName;
    private String storeFileName;
    private Boolean isDone;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
