package cz.vutbr.feec.klaso;

import javax.swing.*;

public class SecondWindow extends JFrame{
    private JPanel PanelMain;
    private JLabel helloHowAreYouLabel;
    private JButton HelBitt;
    private JButton stopPowerPlantButton;
    private JButton sendReportButton;
    private JSlider slider1;
    private JTextArea textArea1;
    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel3;
    private JPanel panel4;
    JFrame frame= new JFrame();
    public SecondWindow()
    {
        System.out.println("starting");
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
        frame.setSize(700,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setContentPane(new SecondWindow().PanelMain);
        frame.add(PanelMain);

        //frame.add(helloHowAreYouLabel);
        frame.setVisible(true);

    }
}
