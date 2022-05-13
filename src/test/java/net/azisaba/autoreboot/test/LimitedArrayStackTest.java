package net.azisaba.autoreboot.test;

import net.azisaba.autoreboot.common.util.LimitedArrayStack;
import org.junit.jupiter.api.Test;

public class LimitedArrayStackTest {
    @Test
    public void test() {
        LimitedArrayStack<Integer> stack = new LimitedArrayStack<>(3);

        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);

        // (size: 3, elements: [2, 3, 4], array: [4, 2, 3])
        assert stack.getAt(0) == 2 : "Actual value: " + stack.getAt(0) + ", entire stack: " + stack;
        assert stack.getAt(1) == 3 : "Actual value: " + stack.getAt(1) + ", entire stack: " + stack;
        assert stack.getAt(2) == 4 : "Actual value: " + stack.getAt(2) + ", entire stack: " + stack;

        // for-each
        stack.forEachIndexed((value, index) -> {
            if (index == 0) {
                assert value == 2 : "Actual value: " + value + ", entire stack: " + stack;
            } else if (index == 1) {
                assert value == 3 : "Actual value: " + value + ", entire stack: " + stack;
            } else if (index == 2) {
                assert value == 4 : "Actual value: " + value + ", entire stack: " + stack;
            } else {
                assert false : "Actual value: " + value + ", entire stack: " + stack;
            }
        });

        int popped = stack.pop();
        assert popped == 4 : "Actual value: " + popped + ", entire stack: " + stack;

        // for-each after popping the element (size: 2, elements: [2, 3], array: [4, 2, 3]) <- array is not changed
        stack.forEachIndexed((value, index) -> {
            if (index == 0) {
                assert value == 2 : "Actual value: " + value + ", entire stack: " + stack;
            } else if (index == 1) {
                assert value == 3 : "Actual value: " + value + ", entire stack: " + stack;
            } else {
                assert false : "Actual value: " + value + ", entire stack: " + stack;
            }
        });

        // iterator
        int index = 0;
        for (int value : stack) {
            if (index == 0) {
                assert value == 2 : "Actual value: " + value + ", entire stack: " + stack;
            } else if (index == 1) {
                assert value == 3 : "Actual value: " + value + ", entire stack: " + stack;
            } else {
                assert false : "Actual value: " + value + ", entire stack: " + stack;
            }
            index++;
        }

        stack.push(4); // overwrites the array[0] with new value

        // (size: 3, elements: [2, 3, 4], array: [4, 2, 3])
        assert stack.peek() == 4 : "Actual value: " + stack.peek() + ", entire stack: " + stack;
        assert stack.getAt(0) == 2 : "Actual value: " + stack.getAt(0) + ", entire stack: " + stack;
        assert stack.getAt(1) == 3 : "Actual value: " + stack.getAt(1) + ", entire stack: " + stack;
        assert stack.getAt(2) == 4 : "Actual value: " + stack.getAt(2) + ", entire stack: " + stack;

        stack.free();

        //assert stack.isEmpty(); // always true, because #clear sets the size to zero

        stack.push(55);
        stack.push(66);
        stack.push(77);

        // (size: 3, elements: [55, 66, 77], array: [55, 66, 77])
        assert stack.getAt(0) == 55 : "Actual value: " + stack.getAt(0) + ", entire stack: " + stack;
        assert stack.getAt(1) == 66 : "Actual value: " + stack.getAt(1) + ", entire stack: " + stack;
        assert stack.getAt(2) == 77 : "Actual value: " + stack.getAt(2) + ", entire stack: " + stack;

        assert stack.indexOf(55) == 0 : "Actual value: " + stack.indexOf(55) + ", entire stack: " + stack;

        assert !stack.contains(4);
        assert stack.contains(55);
        assert !stack.contains(555);

        stack.push(66);

        // (size: 3, elements: [66, 77, 66], array: [66, 66, 77])
        assert stack.peek() == 66 : "Actual value: " + stack.peek() + ", entire stack: " + stack;
        assert stack.indexOf(55) == -1 : "Actual value: " + stack.indexOf(66) + ", entire stack: " + stack;
        assert stack.indexOf(66) == 0 : "Actual value: " + stack.indexOf(66) + ", entire stack: " + stack;
        assert stack.indexOf(77) == 1 : "Actual value: " + stack.indexOf(66) + ", entire stack: " + stack;
        assert stack.lastIndexOf(66) == 2 : "Actual value: " + stack.indexOf(66) + ", entire stack: " + stack;
    }
}
