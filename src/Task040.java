abstract class Gadgets {
    abstract void turnOn();
    abstract void turnOff();
}
// Concrete class implementing the abstract methods
class TVRemote extends Gadgets {
    @Override
    void turnOn() {
        System.out.println("TV is turned ON.");
    }

    @Override
    void turnOff() {
        System.out.println("TV is turned OFF.");
    }
}
class ACRemote extends Gadgets {
    @Override
    void turnOn() {
        System.out.println("AC is turned ON.");
    }

    @Override
    void turnOff() {
        System.out.println("AC is turned OFF.");
    }
}

// Main class to demonstrate abstraction
public class Task040 {
    public static void main(String[] args) {
        Gadgets remote = new TVRemote();
        remote = new ACRemote();
        remote.turnOn();
        remote.turnOff();

//        Gadgets remote = new FanRemote();
//        Gadgets remote = new CoolerRemote();
        remote.turnOn();
        remote.turnOff();
    }
}
