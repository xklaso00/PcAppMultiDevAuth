package cz.vutbr.feec.klaso;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class newAdminGUI extends JFrame{
    JFrame frame= new JFrame();
    private JPanel PanelMain;
    private JPasswordField newAdminField;
    private JButton confirmButton;
    private JLabel textField;

    public newAdminGUI(boolean isLogin)
    {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e)
        { }
        frame.setSize(500,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setContentPane(new SecondWindow().PanelMain);
        frame.add(PanelMain);

        //frame.add(helloHowAreYouLabel);
        frame.setVisible(true);
        if(!isLogin) {
            confirmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    char[] pass = newAdminField.getPassword();
                    PassClass.newAdminPass(pass);
                    System.out.println("Admin created");
                    frame.dispose();
                }
            });
        }
        else
        {
            textField.setText("Put in your admin Password");
            confirmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    char[] pass = newAdminField.getPassword();
                    boolean isit=PassClass.verAdminPass(pass);
                    System.out.println("Admin Logged in"+isit);
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
        }
    }
}
