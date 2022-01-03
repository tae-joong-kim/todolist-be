package com.navercorp.newbie.todolist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoGetResponseDto {
    private Long id;
    private String description;
    private String uploadFileName;
    private String storeFileName;
    private Boolean isDone;
    private LocalDateTime deadline;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
