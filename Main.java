import javax.swing.SwingUtilities;

 public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MyGUI gui = new MyGUI();
            gui.setVisible(true);
        });
    }
}
       
    







