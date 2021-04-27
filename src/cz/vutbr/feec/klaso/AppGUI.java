package cz.vutbr.feec.klaso;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

public class AppGUI extends  JFrame{
    private JButton MDButton;
    public JPanel PanelMain;
    String[] sec={"Security level 1","Security level 2"};
    public JComboBox<String> comboBox1;
    private JLabel LabelSec;
    private JLabel readyLabel;
    private JButton singleAuthButton;
    private JLabel SingleDevLabel;
    private JButton registerBttn;
    private JLabel RegisterLabel;
    private JButton AnotherDevBttn;
    private JLabel SecDevLabel;
    private JPasswordField adminPass;
    private JButton LoginButton;
    private JLabel AdminIn;
    boolean hasAdmin=false;
    newAdminGUI newWindow;
    JFrame frame= new JFrame();
    public void closeMe(){
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    public AppGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.pack();
        frame.add(PanelMain);
        frame.pack();
        frame.setSize(700,500);
        frame.setVisible(true);
        if(PassClass.LoadAdminPass())
            hasAdmin=true;

        if(!hasAdmin)
            newWindow= new newAdminGUI(false);


        Thread t = new Thread() {
            public void run() {
                while(true){

                if(PassClass.isAdminIn())
                {
                    AdminIn.setVisible(true);
                    AdminIn.setForeground(Color.BLUE);
                    AdminIn.setText("Admin Logged in");
                    break;
                }
                else {
                    try {
                        System.out.println("waiting..."+PassClass.isAdminIn());
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            }
        };
        t.start();
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /*try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e)
        {

        }*/
        //frame.setContentPane(new AppGUI().PanelMain);


        //comboBox1= new JComboBox(sec);
        //comboBox1.addItem("Security level 1");
        MDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readyLabel.setText("You can put your phone near the NFC");
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return Terminal.MultiDevAuth(false);
                    }

                    // Can safely update the GUI from this method.
                    protected void done() {
                        boolean status;
                        try {
                            status=get();
                            if(status)
                                readyLabel.setText("Authentication Successful");
                            else
                                readyLabel.setText("Authentication Failed");
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                };
                worker.execute();
                /*boolean auth=Terminal.MultiDevAuth();
                String msg;
                if(auth)
                    msg= "Authentication Successful";
                else
                    msg="Authentication Failed";
                JOptionPane.showMessageDialog(null,msg);
                readyLabel.setText(msg);*/
            }
        });
        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg= (String) comboBox1.getSelectedItem();
                switch (msg) {
                    case "Security level 1":
                        LabelSec.setText("Security level is 1");
                        Options.setSecurityLevel(1);
                        break;
                    case "Security level 2":
                        LabelSec.setText("Security level is 2");
                        Options.setSecurityLevel(2);
                        break;
                }
            }
        });
        singleAuthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SingleDevLabel.setText("You can put your phone near the NFC Reader");

                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return Terminal.SingleDevAuth(true,false);
                    }

                    // Can safely update the GUI from this method.
                    protected void done() {
                        boolean status;
                        try {
                            status=get();
                            if(status)
                            {
                                SingleDevLabel.setText("Authentication Successful");
                                frame.dispose();
                            }

                            else
                                SingleDevLabel.setText("Authentication Failed");
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                };
                worker.execute();
            }
        });
        registerBttn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!PassClass.isAdminIn())
                {
                    RegisterLabel.setText("You have to be logged as an Admin to add users");
                    return;
                }
                RegisterLabel.setText("You can put your phone near the NFC Reader");
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return Terminal.RegisterUser();
                    }

                    // Can safely update the GUI from this method.
                    protected void done() {
                        boolean status;
                        try {
                            status=get();
                            if(status)
                                RegisterLabel.setText("Register Successful");
                            else
                                RegisterLabel.setText("Register Failed");
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                };
                worker.execute();
            }
        });
        AnotherDevBttn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SecDevLabel.setText("Put your phone near the NFC Reader");
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return Terminal.registerAnotherDevice();
                    }
                    // Can safely update the GUI from this method.
                    protected void done() {
                        boolean status;
                        try {
                            status=get();
                            if(status)
                                SecDevLabel.setText("Register Successful");
                            else
                                SecDevLabel.setText("Register Failed");
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                };
                worker.execute();
            }
        });
        LoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newWindow=new newAdminGUI(true);

            }
        });
    }
}
