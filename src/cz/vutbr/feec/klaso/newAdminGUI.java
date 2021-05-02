package cz.vutbr.feec.klaso;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class newAdminGUI extends JFrame  {
    JFrame frame= new JFrame();
    private JPanel PanelMain;
    private JPasswordField newAdminField;
    private JButton confirmButton;
    private JLabel textField;

    public newAdminGUI(boolean isLogin,boolean isAdmin)
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
        //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //frame.setContentPane(new SecondWindow().PanelMain);
        frame.add(PanelMain);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                if(!isAdmin&&isLogin)
                {
                    for(Thread t : Thread.getAllStackTraces().keySet()) {
                        if(t.getName().equals(Options.ThreadName)) {
                            System.out.println("Killing thread");
                            t.interrupt();
                            break;
                        }
                    }
                }
                frame.dispose();
            }
        });
        //frame.add(helloHowAreYouLabel);
        frame.setVisible(true);
        if(!isLogin&&isAdmin) {
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
        else if(isLogin&&isAdmin)
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
        else if(!isLogin)
        {
            textField.setText("Put in your new password");
            confirmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    char[] pass = newAdminField.getPassword();
                    boolean isit=PassClass.addUserPass(Utils.bytesToHex(Options.ActiveID),pass);
                    System.out.println("New Password "+isit);
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
        }
        else
        {
            textField.setText("Put in your password");

            confirmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    char[] pass = newAdminField.getPassword();
                    boolean isit=PassClass.verifyUserPass(Utils.bytesToHex(Options.ActiveID),pass);
                    System.out.println("User is legit? "+isit);

                    for(Thread t : Thread.getAllStackTraces().keySet()) {
                        if(t.getName().equals(Options.ThreadName)) {
                            System.out.println("Killing thread");
                            t.interrupt();
                            break;
                        }
                    }
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
        }
    }
}
