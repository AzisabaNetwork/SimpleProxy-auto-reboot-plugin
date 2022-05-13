package net.azisaba.autoreboot.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Like {@link java.util.Stack}, but with a limited size and new elements are added to the top. When elements are added
 * to the stack, and the stack is full, the oldest element is overwritten. This implementation is not thread-safe.
 * There are several unsupported methods too, mostly because this implementation is index based and unable to delete
 * a specific element (because this is not a list).
 * @param <E> the type of elements
 */
public class LimitedArrayStack<E> extends AbstractCollection<E> {
    private final E[] array;
    private int index = 0;
    private int size = 0;

    /**
     * Constructs a new stack with fixed size.
     * @param size the size
     */
    @SuppressWarnings("unchecked")
    public LimitedArrayStack(int size) {
        this((E[]) new Object[size]);
    }

    /**
     * Constructs a new stack with the given array. Maximum size of the stack is the length of the array.
     * @param array the array
     */
    public LimitedArrayStack(E @NotNull [] array) {
        Objects.requireNonNull(array, "array is null");
        this.array = array;
    }

    /**
     * Adds the element to the stack.
     * @param value the value
     */
    @Contract(mutates = "this")
    public void push(E value) {
        array[index] = value;
        index = (index + 1) % array.length;
        size = Math.min(size + 1, array.length);
    }

    /**
     * Adds the element to the stack.
     * @param e element whose presence in this collection is to be ensured
     * @return always true; the operation will never fail
     */
    @Contract(mutates = "this")
    @Override
    public boolean add(E e) {
        push(e);
        return true;
    }

    /**
     * Removes the last element. In this case, instead of removing the element, the index will be set to the previous
     * index.
     * @return the removed element
     */
    @Contract(mutates = "this")
    public E pop() {
        size = Math.max(size - 1, 0);
        index = (index - 1 + array.length) % array.length;
        return array[index];
    }

    @Contract("_ -> fail")
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("#remove is not supported. Use #pop to remove the last element.");
    }

    @Contract("_ -> fail")
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException("#removeIf is not supported. Use #pop to remove the last element.");
    }

    @Contract("_ -> fail")
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("#removeAll is not supported. Use #pop to remove the last element.");
    }

    /**
     * Returns the element at current index.
     * @return the element
     */
    public E peek() {
        if (size == 0) {
            return null;
        }
        return getAt(size - 1);
    }

    /**
     * the true size of the stack
     * @return the size
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns the index where the element is stored.
     * @param o the value
     * @return the index; -1 if not found
     * @throws NullPointerException if {@code o} is {@code null}
     */
    public int indexOf(Object o) {
        for (int i = 0; i < size; i++) {
            if (o.equals(getAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index where the element is stored, but in reverse order (searches from tail).
     * @param o the value
     * @return the index; -1 if not found
     * @throws NullPointerException if {@code o} is {@code null}
     */
    public int lastIndexOf(Object o) {
        for (int i = size - 1; i >= 0; i--) {
            if (o.equals(getAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the element at the index. May return {@code null} if the element at specified index is not set.
     * @param index the index
     * @return the element at the index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public E getAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
        }
        return array[(index + this.index + (array.length - size) + array.length) % array.length];
    }

    @Override
    public void forEach(@NotNull Consumer<? super E> action) {
        Objects.requireNonNull(action, "action is null");
        for (int i = 0; i < size; i++) {
            action.accept(getAt(i));
        }
    }

    /**
     * Like {@link #forEach(Consumer)}, but the index will be provided with the value to BiConsumer.
     * @param action The action to be performed for each element (first argument is the value, second is the index)
     */
    public void forEachIndexed(@NotNull BiConsumer<? super E, Integer> action) {
        Objects.requireNonNull(action, "action is null");
        for (int i = 0; i < size; i++) {
            action.accept(getAt(i), i);
        }
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int curr = -1;

            private boolean isLast() {
                return curr == size - 1;
            }

            @Override
            public boolean hasNext() {
                return curr < size - 1;
            }

            @Override
            public E next() {
                if (isLast()) {
                    throw new NoSuchElementException("No more elements (index: " + curr + ", size: " + size + ")");
                }
                return getAt(++curr);
            }
        };
    }

    /**
     * Clears<small>*1</small> the stack.
     * <p>
     * <small>*1: Resets the size and index to zero. Does not free the backing array.</small>
     */
    @Contract(mutates = "this")
    @Override
    public void clear() {
        size = 0;
        index = 0;
    }

    /**
     * Works like {@link #clear()}, but also frees the backing array by filling the array with null.
     */
    @Contract(mutates = "this")
    public void free() {
        clear();
        Arrays.fill(array, null);
    }
}
