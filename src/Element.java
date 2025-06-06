import java.util.HashMap;
import java.util.Map;

// The Element enum definition.
// For demonstration, it's placed in the same file. In a real project,
// a public enum like 'Element' would typically be in its own file named 'Element.java'.
public enum Element {
    H("Hydrogen", 1, 1.008f),
    HE("Helium", 2, 4.0026f),
    LI("Lithium", 3, 6.94f), // Added for fuller demonstration
    BE("Beryllium", 4, 9.0122f), // Added for fuller demonstration
    B("Boron", 5, 10.81f),    // Added for fuller demonstration
    C("Carbon", 6, 12.011f),  // Added for fuller demonstration
    N("Nitrogen", 7, 14.007f),// Added for fuller demonstration
    O("Oxygen", 8, 15.999f),  // Added for fuller demonstration
    F("Fluorine", 9, 18.998f),// Added for fuller demonstration
    NE("Neon", 10, 20.180f);

    private static final Map<String, Element> BY_LABEL = new HashMap<>();
    private static final Map<Integer, Element> BY_ATOMIC_NUMBER = new HashMap<>();
    private static final Map<Float, Element> BY_ATOMIC_WEIGHT = new HashMap<>();

    static {
        for (Element e : values()) {
            BY_LABEL.put(e.label, e);
            BY_ATOMIC_NUMBER.put(e.atomicNumber, e);
            BY_ATOMIC_WEIGHT.put(e.atomicWeight, e);
        }
    }

    public final String label;
    public final int atomicNumber;
    public final float atomicWeight;

    private Element(String label, int atomicNumber, float atomicWeight) {
        this.label = label;
        this.atomicNumber = atomicNumber;
        this.atomicWeight = atomicWeight;
    }

    public static Element valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }

    public static Element valueOfAtomicNumber(int number) {
        return BY_ATOMIC_NUMBER.get(number);
    }

    public static Element valueOfAtomicWeight(float weight) {
        return BY_ATOMIC_WEIGHT.get(weight);
    }
}