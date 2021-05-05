package cz.vutbr.feec.klaso;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IDwindow extends JFrame{
    private JPanel PanelMain;
    private JTextArea IDInput;
    private JButton ConfirmBttn;
    private JButton closeButton;
    private JLabel textHint;
    private JLabel FinalText;
    JFrame frame= new JFrame();
    public IDwindow(){
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
        frame.setTitle("User Deletion");
        frame.add(PanelMain);
        frame.pack();
        frame.setSize(400,250);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ConfirmBttn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String IDToDel="";
                try{
                    IDToDel=IDInput.getText();
                }catch (Exception exception){
                    FinalText.setText("Empty ID.");
                    return;
                }
                if(IDToDel.length()!=10){
                    System.out.println("Length is "+IDToDel.length());
                    FinalText.setText("ID length must be 10");
                    return;
                }
                byte[] IDBytes=Utils.hexStringToByteArray(IDToDel);
                boolean wasDeleted=Options.DelUser(IDBytes);
                if(wasDeleted)
                    FinalText.setText("ID was deleted");
                else
                    FinalText.setText("ID could not be deleted");
                IDInput.setText("");

            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }
}
