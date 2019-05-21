package me.hiten.getandroidx.fragment;


import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainFragment {


    private JTextField editText;
    private JList resultList;
    private JPanel rootView;

    public void show() {
        JFrame frame = new JFrame("Search AndroidX Packages");
        frame.setContentPane(rootView);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.pack();
        frame.setSize(600,600);
        frame.setVisible(true);
        setListener();
    }

    private void setListener() {

        editText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                System.out.println(editText.getText());
            }
        });

        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setLayoutOrientation(JList.VERTICAL);
    }


    public static void main(String[] args){
        MainFragment mainFragment = new MainFragment();
        mainFragment.show();
    }

}
