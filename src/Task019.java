public class Task019 {

    public static void main(String[] args) {




        for (Element element : Element.values()) {

            System.out.printf("Symbol: %s, Label: %s, Atomic Number: %d, Atomic Weight: %.4f%n",
                    element.name(),
                    element.label,
                    element.atomicNumber,
                    element.atomicWeight);
        }

        System.out.println("\n--- Demonstrating Element lookup methods (from Task016_1) ---");


        Element carbon = Element.valueOfLabel("Carbon");
        if (carbon != null) {
            System.out.printf("Lookup by Label 'Carbon': Found %s (Atomic Number: %d)%n",
                    carbon.name(), carbon.atomicNumber);
        } else {
            System.out.println("Element 'Carbon' not found by label.");
        }


        Element nitrogen = Element.valueOfAtomicNumber(7);
        if (nitrogen != null) {
            System.out.printf("Lookup by Atomic Number 7: Found %s (Label: %s)%n",
                    nitrogen.name(), nitrogen.label);
        } else {
            System.out.println("Element with Atomic Number 7 not found.");
        }

        Element nonExistent = Element.valueOfLabel("ImaginaryElement");
        if (nonExistent == null) {
            System.out.println("Lookup for 'ImaginaryElement': Result is null (as expected).");
        }
    }
}
