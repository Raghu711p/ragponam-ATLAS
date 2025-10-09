import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample test class with slow-running tests to demonstrate timeout functionality.
 */
public class SlowTest {
    
    @Test
    @DisplayName("Quick test")
    void testQuick() {
        assertEquals(1, 1);
    }
    
    @Test
    @DisplayName("Medium speed test")
    void testMedium() {
        try {
            Thread.sleep(100); // 100ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(2, 2);
    }
    
    @Test
    @DisplayName("Slow test")
    void testSlow() {
        try {
            Thread.sleep(2000); // 2 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(3, 3);
    }
    
    @Test
    @DisplayName("Very slow test")
    void testVerySlow() {
        try {
            Thread.sleep(10000); // 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(4, 4);
    }
}