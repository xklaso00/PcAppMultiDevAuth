package cz.vutbr.feec.klaso;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;


public class newAdminGUI extends JFrame  {
    JFrame frame= new JFrame();
    private JPanel PanelMain;
    private JPasswordField newAdminField;
    private JButton confirmButton;
    private JLabel textField;
    private JPasswordField confirmPassField;
    private JLabel labelPas;
    private JLabel ConfirmPassLabel;
    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel3;

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


        //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //frame.setContentPane(new SecondWindow().PanelMain);
        frame.setTitle("Login Screen");
        try{
            frame.setLayout(new BorderLayout());
            JLabel background=new JLabel(new ImageIcon("backgroundSmall.jpg"));
            frame.add(background);
            //background.setSize(700,500);
            background.setLayout(new GridLayout());
            background.add(PanelMain);
            PanelMain.setOpaque(false);
            panel1.setOpaque(false);
            panel2.setOpaque(false);
            panel3.setOpaque(false);
        }catch (Exception e){
            System.out.println("Error in loading the background");
            frame.add(PanelMain);
        }



        //frame.add(PanelMain);
        frame.pack();
        frame.setSize(500,300);
        frame.setResizable(false);
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
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        if(!isLogin&&isAdmin) {
            confirmButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    char[] pass = newAdminField.getPassword();
                    char[] pass2 = confirmPassField.getPassword();

                    if(!Arrays.equals(pass,pass2)){
                        textField.setText("Both Passwords must be the same");
                        return;
                    }

                    PassClass.newAdminPass(pass);
                    System.out.println("Admin created");
                    frame.dispose();
                }
            });
        }
        else if(isLogin&&isAdmin)
        {
            confirmPassField.setVisible(false);
            ConfirmPassLabel.setVisible(false);
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
                    char[] pass= newAdminField.getPassword();
                    char[] pass2 = confirmPassField.getPassword();
                    if(!Arrays.equals(pass,pass2)){
                        textField.setText("Both Passwords must be the same");
                        return;
                    }
                    boolean isit=PassClass.addUserPass(Utils.bytesToHex(Options.ActiveID),pass);
                    System.out.println("New Password "+isit);
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
        }
        else
        {
            confirmPassField.setVisible(false);
            ConfirmPassLabel.setVisible(false);
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
