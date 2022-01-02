package com.navercorp.newbie.todolist.controller;

import com.navercorp.newbie.todolist.domain.Todo;
import com.navercorp.newbie.todolist.config.ObjectStorage;
import com.navercorp.newbie.todolist.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final TodoService todoService;
    private final ObjectStorage objectStorage;

    @GetMapping("/files/{fileName}")
    public ResponseEntity<byte[]> getFileResource(@PathVariable String fileName) {
        Todo todo = todoService.readTodoByStoreFileName(fileName);

        String storeFileName = todo.getStoreFileName();
        String contentType = todo.getContentType();

        byte[] bytes = objectStorage.fileDownload(storeFileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
