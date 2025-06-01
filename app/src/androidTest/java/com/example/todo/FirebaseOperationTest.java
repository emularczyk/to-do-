package com.example.todo;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FirebaseOperationTest {

    private Context context;
    private TestRepository testRepository;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testRepository = new TestRepository();
    }

    @Test
    public void whenAddingTodo_thenItShouldBeStored() {
        // given
        Todo todo = new Todo("test_id", "Test content");

        // when
        boolean result = testRepository.saveTodo(todo);

        // then
        assertTrue(result);
        assertEquals(todo, testRepository.getTodo("test_id"));
    }

    @Test
    public void whenLoadingTodos_thenShouldReturnAllTodos() {
        // given
        testRepository.saveTodo(new Todo("id1", "Content 1"));
        testRepository.saveTodo(new Todo("id2", "Content 2"));

        // when
        int count = testRepository.getTodosCount();

        // then
        assertEquals(2, count);
    }
}