package com.example.mmmmeeting.activity;


//generic stack class with the added functionality of peeking the second to top element.
// hacky way of typing the array, so don't return the stack array ever

public class Stack<T> {
    private T[] stack;
    private int size;

    public Stack(int maxSize) {
        stack = (T[])new Object[maxSize];
        size = 0;
    }

    public boolean push(T obj){
        if(size >= stack.length)
            return false;
        stack[size] = obj;
        size++;
        return true;
    }

    public T pop() {
        if(isEmpty())
            throw new IndexOutOfBoundsException();
        size--;
        T out = stack[size];
        stack[size] = null;
        return out;
    }

    public T peek() {
        return stack[size - 1];
    }

    //peek the element directly under the topmost
    public T peekUnder() {
        return stack[size - 2];
    }

    public T get(int index) {
        if(index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        return stack[index];
    }

    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size == 0;
    }
}
