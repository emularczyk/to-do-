package com.example.todo;

import android.content.Context;
import android.widget.Toast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoAdapterTest {

    @Mock
    private Context mockContext;

    private TodoAdapter todoAdapter;
    private ArrayList<Todo> todoList;
    private static final String TEST_DIRECTORY_ID = "test_dir_id";
    private static final String TEST_USER_ID = "test_user_id";

    @Before
    public void setUp() {
        todoList = new ArrayList<>();
        todoAdapter = new TodoAdapter(todoList, mockContext, TEST_DIRECTORY_ID, TEST_USER_ID);
    }

    @Test
    public void whenAddNewTodo_thenListSizeIncreases() {
        // given
        Todo todo = new Todo("1", "Test todo");
        assertEquals(0, todoList.size());

        // when
        todoList.add(todo);

        // then
        assertEquals(1, todoList.size());
        assertEquals("Test todo", todoList.get(0).getContent());
    }

    @Test
    public void whenGetItemCount_thenReturnsCorrectSize() {
        // given
        todoList.add(new Todo("1", "First todo"));
        todoList.add(new Todo("2", "Second todo"));

        // when
        int count = todoAdapter.getItemCount();

        // then
        assertEquals(2, count);
    }

    @Test
    public void whenTodoHasImage_thenImageIsNotNull() {
        // given
        Todo todo = new Todo("1", "Todo with image", "base64image");

        // when
        String image = todo.getImage();

        // then
        assertNotNull(image);
        assertEquals("base64image", image);
    }
}