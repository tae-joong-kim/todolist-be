package com.navercorp.newbie.todolist.service;

import com.navercorp.newbie.todolist.config.FileSafer;
import com.navercorp.newbie.todolist.config.ObjectStorage;
import com.navercorp.newbie.todolist.domain.Todo;
import com.navercorp.newbie.todolist.dto.TodoCreateForm;
import com.navercorp.newbie.todolist.dto.TodoUpdateForm;
import com.navercorp.newbie.todolist.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TodoService {
    private final TodoRepository todoRepository;
    private final ObjectStorage objectStorage;
    private final FileSafer filesafer;

    public boolean fileUpload(MultipartFile multipartFile, String storeFileName) throws Exception {
        if (multipartFile != null) {
            String uploadFileName = multipartFile.getOriginalFilename();
            log.info("service.fileUpload - success");
            if (uploadFileName != null) {
                log.info("service.fileUpload - success - success");
                if (filesafer.FileCheck(multipartFile.getBytes(), multipartFile.getOriginalFilename(), null) == true) {
                    log.info("service.fileUpload - success - success - storage start");
                    objectStorage.fileUpload(multipartFile, storeFileName);
                    log.info("service.fileUpload - success - success - storage end");
                    return true;
                }else {
                    log.info("service.fileUpload - success - failed");
                }
            }
        }else{
            log.info("service.fileUpload - failed");
        }
        return false;
    }


    public Todo createTodo(TodoCreateForm todoCreateForm) throws Exception {
        Todo todo = new Todo();
        todo.create(todoCreateForm);

        log.info("service.createTodo = {}", todo);

        MultipartFile multipartFile = todoCreateForm.getFile();
        String storeFileName = todo.getStoreFileName();

        log.info("sevice-createTodo -> service-fileUpload");
        if(fileUpload(multipartFile, storeFileName) == false) {
            log.trace("service-fileUpload return false");
            todo.setFileName();
        }else{
            log.info("service-fileUpload return true");
        }

        log.info("sevice-createTodo -> repository-save");
        Todo save = todoRepository.save(todo);
        log.info("Todo saved = {}", save);

        return save;
    }

    @Transactional(readOnly = true)
    public List<Todo> readTodoAsList(){
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Todo readDetailTodo(Long id){
        return todoRepository.findById(id).get();
    }

    public Todo updateTodo(Long id, TodoUpdateForm todoUpdateForm) throws Exception {
        Todo todo = todoRepository.findById(id).get();
        todo.update(todoUpdateForm);

        MultipartFile multipartFile = todoUpdateForm.getFile();
        String storeFileName = todo.getStoreFileName();

        if(fileUpload(multipartFile, storeFileName) == false)
            todo.setFileName();

        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id){
        todoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Todo readTodoByStoreFileName(String storeFileName) {
        return todoRepository.findByStoreFileName(storeFileName).get();
    }
}
