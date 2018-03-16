package PackageRMI;

public class Control implements ControlInterface {

    public Control(){

    }

    @Override
    public boolean say_this(String text) {
        System.out.println(text);
        return true;
    }
}
