import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample test class for testing the JUnit test runner.
 * Contains various types of tests including passing, failing, and exception tests.
 */
public class SampleCalculatorTest {
    
    private SampleCalculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new SampleCalculator();
    }
    
    @Test
    @DisplayName("Addition of positive numbers")
    void testAddPositive() {
        assertEquals(5, calculator.add(2, 3));
        assertEquals(10, calculator.add(7, 3));
    }
    
    @Test
    @DisplayName("Addition with negative numbers")
    void testAddNegative() {
        assertEquals(-1, calculator.add(-3, 2));
        assertEquals(-5, calculator.add(-2, -3));
    }
    
    @Test
    @DisplayName("Addition with zero")
    void testAddZero() {
        assertEquals(5, calculator.add(5, 0));
        assertEquals(0, calculator.add(0, 0));
    }
    
    @Test
    @DisplayName("Subtraction test")
    void testSubtract() {
        assertEquals(2, calculator.subtract(5, 3));
        assertEquals(-2, calculator.subtract(3, 5));
    }
    
    @Test
    @DisplayName("Multiplication test")
    void testMultiply() {
        assertEquals(6, calculator.multiply(2, 3));
        assertEquals(0, calculator.multiply(5, 0));
        assertEquals(-10, calculator.multiply(-2, 5));
    }
    
    @Test
    @DisplayName("Division test")
    void testDivide() {
        assertEquals(2.0, calculator.divide(6, 3), 0.001);
        assertEquals(2.5, calculator.divide(5, 2), 0.001);
    }
    
    @Test
    @DisplayName("Division by zero should throw exception")
    void testDivideByZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.divide(5, 0);
        });
    }
    
    @Test
    @DisplayName("Even number test")
    void testIsEven() {
        assertTrue(calculator.isEven(4));
        assertTrue(calculator.isEven(0));
        assertFalse(calculator.isEven(3));
        assertFalse(calculator.isEven(-3));
        assertTrue(calculator.isEven(-4));
    }
    
    @Test
    @DisplayName("Factorial test")
    void testFactorial() {
        assertEquals(1, calculator.factorial(0));
        assertEquals(1, calculator.factorial(1));
        assertEquals(2, calculator.factorial(2));
        assertEquals(6, calculator.factorial(3));
        assertEquals(24, calculator.factorial(4));
        assertEquals(120, calculator.factorial(5));
    }
    
    @Test
    @DisplayName("Factorial with negative number should throw exception")
    void testFactorialNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.factorial(-1);
        });
    }
    
    @Test
    @DisplayName("This test will fail intentionally")
    void testIntentionalFailure() {
        // This test is designed to fail to test failure handling
        assertEquals(10, calculator.add(2, 3)); // Should be 5, not 10
    }
}