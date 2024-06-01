package paint;

public class Main {
    public static void main(String[] args) {
        try {
            // Tu código de inicio aquí
            System.out.println("Aplicación iniciada");
            new VISTA().setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
