package cz.vutbr.feec.klaso;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
    private JButton removeSecondaryDeviceButton;
    private JLabel removeSecText;
    private JButton DelUserBtt;
    private JLabel RemoveUserText;
    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel3;

    boolean hasAdmin=false;
    newAdminGUI newWindow;
    SecondWindow sw;
    byte SecDevNumber=(byte)0x01;
    JFrame frame= new JFrame("Multi Device Authentication Application");
    public void closeMe(){
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }






    public AppGUI()  {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            frame.setLayout(new BorderLayout());
            JLabel background=new JLabel(new ImageIcon("background2.jpg"));
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


        //Options.loadPrivateKey();
        frame.pack();

        frame.setSize(700,500);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
        //Options.savePrivateKey();
        //Options.loadPrivateKey();
        if(PassClass.LoadAdminPass())
            hasAdmin=true;

        if(!hasAdmin) {
            newWindow = new newAdminGUI(false, true);
            Options.generateKeys();
        }
        //ECOperations.GenerateTimeStamp();

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
                            if(status) {
                                readyLabel.setText("Authentication Successful");
                                sw=new SecondWindow();
                                frame.dispose();
                            }
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
                    case "Security level 0":
                        LabelSec.setText("Security level is 0");
                        Options.setSecurityLevel(0);
                        break;

                }
            }
        });
        singleAuthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*Thread legitUserThread = new Thread() {
                    public void run() {

                        while(true){
                            if(PassClass.isCurrentUserLegit())
                            {
                                sw=new SecondWindow();
                                frame.dispose();
                                break;
                            }
                            else {
                                try {
                                    sleep(1000);
                                    System.out.println("Still alive");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    if(PassClass.isCurrentUserLegit())
                                    {
                                        sw=new SecondWindow();
                                        frame.dispose();
                                    }
                                    else {
                                        SingleDevLabel.setText("Wrong Password");
                                    }
                                    break;
                                }
                            }
                        }
                    }
                };*/
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
                                SingleDevLabel.setText("Authentication waiting for Password");
                                CheckPass cp=new CheckPass(1);
                                cp.start();
                                Options.ThreadName=cp.getName();
                               // legitUserThread.start();
                                //Options.ThreadName=legitUserThread.getName();

                                newWindow= new newAdminGUI(true,false);



                                //frame.dispose();
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
                            if(status) {
                                RegisterLabel.setText("Register done");
                                newWindow= new newAdminGUI(false,false);
                            }
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
                newWindow=new newAdminGUI(true,true);

            }
        });
        removeSecondaryDeviceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSecText.setText("You can put your phone on the reader");
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
                                removeSecText.setText("Authentication waiting for Password");
                                CheckPass cp=new CheckPass(2);
                                cp.start();
                                Options.ThreadName=cp.getName();
                                newWindow= new newAdminGUI(true,false);
                            }
                            else
                                removeSecText.setText("Authentication Failed");
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
        DelUserBtt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!PassClass.isAdminIn())
                {
                    RemoveUserText.setText("You have to be logged as an Admin to remove users");
                    return;
                }
                IDwindow iDwindow=new IDwindow();
            }
        });
    }
    class CheckPass extends Thread {
        int option;//option 1:singleDevAuth, option 2:Deregister 2nd Device
        CheckPass(int option) {
            this.option=option;
        }
        boolean exit = false;
        public void run() {
            while(true){
                if(PassClass.isCurrentUserLegit())
                {
                    sw=new SecondWindow();
                    frame.dispose();
                    break;
                }
                else {
                    try {
                        sleep(1000);
                        System.out.println("Still alive");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        if(PassClass.isCurrentUserLegit()&&option==1)
                        {
                            sw=new SecondWindow();
                            frame.dispose();
                        }
                        else if(PassClass.isCurrentUserLegit()&&option==2){
                            boolean passed=Terminal.DeregisterSecondaryDevice(SecDevNumber);
                            if (passed)
                                removeSecText.setText("Secondary device removed.");
                            else
                                removeSecText.setText("Could not remove secondary device.");
                            PassClass.unLog();
                        }
                        else {
                            SingleDevLabel.setText("Wrong Password");
                        }
                        break;
                    }
                }
            }
        }
    }
}
