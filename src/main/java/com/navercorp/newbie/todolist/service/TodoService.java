package com.navercorp.newbie.todolist.service;

import com.navercorp.newbie.todolist.config.ObjectStorage;
import com.navercorp.newbie.todolist.domain.Todo;
import com.navercorp.newbie.todolist.dto.TodoCreateForm;
import com.navercorp.newbie.todolist.dto.TodoUpdateForm;
import com.navercorp.newbie.todolist.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final ObjectStorage objectStorage;

    public void fileUpload(MultipartFile multipartFile, String storeFileName) throws IOException {
        if (multipartFile != null) {
            String uploadFileName = multipartFile.getOriginalFilename();
            if (uploadFileName != null){
                objectStorage.fileUpload(multipartFile, storeFileName);
            }
        }
    }

    public Todo createTodo(TodoCreateForm todoCreateForm) throws IOException {
        Todo todo = new Todo();
        todo.create(todoCreateForm);

        MultipartFile multipartFile = todoCreateForm.getMultipartFile();
        String storeFileName = todo.getStoreFileName();

        fileUpload(multipartFile, storeFileName);

        return todoRepository.save(todo);
    }

    @Transactional(readOnly = true)
    public List<Todo> readTodo(){
        return todoRepository.findAll();
    }

    public Todo updateTodo(Long id, TodoUpdateForm todoUpdateForm) throws IOException {
        Todo todo = todoRepository.findById(id).get();
        todo.update(todoUpdateForm);

        MultipartFile multipartFile = todoUpdateForm.getMultipartFile();
        String storeFileName = todo.getStoreFileName();

        fileUpload(multipartFile, storeFileName);

        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id){
        todoRepository.deleteById(id);
    }

    public Todo readTodoByStoreFileName(String storeFileName) {
        return todoRepository.findByStoreFileName(storeFileName).get();
    }
}
