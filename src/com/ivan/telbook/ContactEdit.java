package com.ivan.telbook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.ivan.telbook.Contact;
import java.sql.*;

public class ContactEdit extends JDialog implements WindowListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tfName1;
    private JTextField tfName2;
    private JTextField tfName3;
    private JTextField tfTel;
    private JTextField tfAddress;
    public Contact m_Contact;
    public Connection m_Connection;

    public ContactEdit() {
        m_Contact = null;
        m_Connection = null;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {

        m_Contact.m_strName1 = tfName1.getText();
        m_Contact.m_strName2 = tfName2.getText();
        m_Contact.m_strName3 = tfName3.getText();
        m_Contact.m_strTel = tfTel.getText();
        m_Contact.m_strAddress = tfAddress.getText();

        m_Contact.Save(m_Connection);

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ContactEdit dialog = new ContactEdit();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }


    public void windowOpened(WindowEvent e){
        System.out.println("windowOpened");

        centreWindow(this);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        System.out.println("closed");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("closing");
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        System.out.println("deactivated");
    }

    @Override
    public void windowActivated(WindowEvent e) {
        System.out.println("activated");

        if(m_Contact == null) {
            this.setTitle("Нов контакт");
            m_Contact = new Contact();
        }
        else {
            this.setTitle("Редакция на контакт");

            tfName1.setText(m_Contact.m_strName1);
            tfName2.setText(m_Contact.m_strName2);
            tfName3.setText(m_Contact.m_strName3);
            tfTel.setText(m_Contact.m_strTel);
            tfAddress.setText(m_Contact.m_strAddress);
        }
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        System.out.println("deiconified");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        System.out.println("iconified");
    }

    public static void centreWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }
}