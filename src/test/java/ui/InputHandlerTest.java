package ui;

import org.junit.jupiter.api.Test;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class InputHandlerTest {

    @Test
    void 숫자_즉시_반환() {
        assertEquals(2, handler("2\n").readInt());
    }

    @Test
    void 비숫자_입력후_숫자_반환() {
        assertEquals(3, handler("abc\n3\n").readInt());
    }

    @Test
    void 여러번_비숫자_후_숫자_반환() {
        assertEquals(1, handler("!!\nfoo\n1\n").readInt());
    }

    @Test
    void exit_소문자_음수1_반환() {
        assertEquals(-1, handler("exit\n").readInt());
    }

    @Test
    void EXIT_대문자_음수1_반환() {
        assertEquals(-1, handler("EXIT\n").readInt());
    }

    @Test
    void isExit_exit_true() {
        assertTrue(handler("").isExit("exit"));
    }

    @Test
    void isExit_Exit_대소문자무관_true() {
        assertTrue(handler("").isExit("Exit"));
    }

    @Test
    void isExit_숫자_false() {
        assertFalse(handler("").isExit("1"));
    }

    @Test
    void isExit_공백포함_true() {
        assertTrue(handler("").isExit("  exit  "));
    }

    private InputHandler handler(String input) {
        return new InputHandler(
            new Scanner(input),
            new PrintStream(OutputStream.nullOutputStream())
        );
    }
}
