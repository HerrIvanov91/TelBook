package com.ivan.telbook;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.lang.invoke.StringConcatFactory;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.io.*;
import com.ivan.telbook.Contact;
import com.ivan.telbook.ContactEdit;
import java.awt.Window;
import java.awt.Dimension;
import java.awt.Toolkit;

class NonEditableTableModel extends DefaultTableModel {

    public boolean isCellEditable(int row, int column) {
        return false;
    }
}

public class TelBookView extends JDialog implements WindowListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonQuit;
    private JTable tableContacts;
    private JButton buttonNew;
    private JButton buttonEdit;
    private JButton buttonDelete;
    private JButton buttonUpdateStruct;
    private JButton buttonFilter;
    private JTextField tfName1;
    private JTextField tfName2;
    private JTextField tfName3;
    private NonEditableTableModel m_TableModel;
    private Connection m_Connection;
    private ContactContainer m_Contacts = new ContactContainer();

    public TelBookView() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonNew);

        buttonQuit.addActionListener(new ActionListener() {
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
        buttonDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DeleteSelectedRow();
            }
        });
        buttonEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditContact();
            }
        });
        buttonNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddContact();
            }
        });
        buttonUpdateStruct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateStruct();
            }
        });
        buttonFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FilterContacts();
            }
        });
    }

    @Override
    public void windowOpened(WindowEvent e){
        System.out.println("windowOpened");

        centreWindow(this);

        this.setTitle("Телефонен указател");

        m_TableModel = new NonEditableTableModel();
        m_TableModel.addColumn("Име");
        m_TableModel.addColumn("Презиме");
        m_TableModel.addColumn("Фамилия");
        m_TableModel.addColumn("Телефон");
        m_TableModel.addColumn("Адрес");
        tableContacts.setModel(m_TableModel);
        tableContacts.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        GetContacts();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        System.out.println("closed");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("closing");
        this.dispose();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        System.out.println("deactivated");
    }

    @Override
    public void windowActivated(WindowEvent e) {
        System.out.println("activated");
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        System.out.println("deiconified");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        System.out.println("iconified");
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        TelBookView dialog = new TelBookView();
        dialog.pack();
        dialog.addWindowListener(dialog);
        dialog.setVisible(true);
        System.exit(0);
    }

    private void GetContacts()
    {
        String strHost = "";
        String strPort = "";
        String strDBName = "";
        String strDBUser = "";
        String strDBPass = "";
        //---------------------------------------------------
        List<String> list = new ArrayList<String>();
        File file = new File("telbook.ini");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                list.add(text);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        if(list.size() == 5) {
            strHost = list.get(0);
            strPort = list.get(1);
            strDBName = list.get(2);
            strDBUser = list.get(3);
            strDBPass = list.get(4);
        }

        else
        {
            JOptionPane.showMessageDialog(this, "Invalid telbook.ini file structure - must have 5 lines - host, port, dbname, dbuser, dbpass!", "Incorrect ini file struct",
                    JOptionPane.WARNING_MESSAGE);
        }
        //---------------------------------------------------

        String strURL = "";
        strURL = String.format("jdbc:postgresql://%s:%s/%s",strHost, strPort, strDBName);
        m_Connection = null;
        try {
            //Class.forName("org.postgresql.Driver");

            m_Connection = DriverManager
                    .getConnection(strURL,
                            strDBUser, strDBPass);
            m_Connection.setAutoCommit(true);
            System.out.println("Opened database successfully");

            m_Contacts.Load(m_Connection, "WHERE 1=1");
            m_Contacts.FillTableModel(m_TableModel);
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(null, e.getClass().getName()+": "+ e.getMessage(), "JDBC ERROR: ", JOptionPane.ERROR_MESSAGE);
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        }
        System.out.println("Operation done successfully");
    }

    private void DeleteSelectedRow()
    {
        int nSelectedRow = tableContacts.getSelectedRow();

        Contact lpItem = null;
        if(nSelectedRow >= 0)
        {
            lpItem = this.m_Contacts.get(nSelectedRow);
            if(lpItem.DeleteItem(this.m_Connection)) {
                m_Contacts.remove(nSelectedRow);
                this.m_TableModel.removeRow(nSelectedRow);
            }
        }
    }

    private void EditContact()
    {
        int nSelectedRow = tableContacts.getSelectedRow();

        Contact lpItem = null;
        if(nSelectedRow >= 0) {
            lpItem = this.m_Contacts.get(nSelectedRow);

            ContactEdit dlgEdit = new ContactEdit();
            dlgEdit.m_Contact = lpItem;
            dlgEdit.m_Connection = m_Connection;
            dlgEdit.pack();
            dlgEdit.addWindowListener(dlgEdit);
            dlgEdit.setVisible(true);
            dlgEdit.toFront();

            m_TableModel.setValueAt(lpItem.m_strName1, nSelectedRow, 0);
            m_TableModel.setValueAt(lpItem.m_strName2, nSelectedRow, 1);
            m_TableModel.setValueAt(lpItem.m_strName3, nSelectedRow, 2);
            m_TableModel.setValueAt(lpItem.m_strTel, nSelectedRow, 3);
            m_TableModel.setValueAt(lpItem.m_strAddress, nSelectedRow, 4);
        }
    }

    private void AddContact()
    {
        ContactEdit dlgEdit = new ContactEdit();
        dlgEdit.m_Contact = null;
        dlgEdit.m_Connection = m_Connection;
        dlgEdit.pack();
        dlgEdit.addWindowListener(dlgEdit);
        dlgEdit.setVisible(true);
        dlgEdit.toFront();

        if(dlgEdit.m_Contact.m_nID > 0) {
            m_Contacts.add(dlgEdit.m_Contact);
            m_TableModel.insertRow(m_TableModel.getRowCount(), new Object[]{dlgEdit.m_Contact.m_strName1, dlgEdit.m_Contact.m_strName2, dlgEdit.m_Contact.m_strName3, dlgEdit.m_Contact.m_strTel, dlgEdit.m_Contact.m_strAddress});
        }
    }

    public static void centreWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private void UpdateStruct()
    {
        if(m_Connection == null)
            return;

        String strQuery = "";
        strQuery = String.format("SELECT EXISTS (\n" +
                "   SELECT 1 \n" +
                "   FROM   pg_tables\n" +
                "   WHERE  schemaname = 'public'\n" +
                "   AND    tablename = 'person'\n" +
                "   );");

        boolean bTableExists = false;
        Statement stmt = null;
        try {
            stmt = m_Connection.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            while ( rs.next() ) {
                bTableExists = rs.getBoolean(1);
            }
        }
        catch (Exception e){
            return;
        }

        if(bTableExists)
            return;

        strQuery = String.format("CREATE TABLE public.Person\n" +
                                "(\n" +
                                "    id SERIAL PRIMARY KEY,\n" +
                                "    name1 character varying(256),\n" +
                                "    name2 character varying(256),\n" +
                                "    name3 character varying(256),\n" +
                                "    tel character varying(20),\n" +
                                "    address character varying(100)\n" +
                                ")\n" +
                                "WITH (\n" +
                                "    OIDS = FALSE\n" +
                                ");");

        try {
            stmt = m_Connection.createStatement();
            stmt.execute(strQuery);
        }
        catch (Exception e){
            return;
        }
    }

    private void FilterContacts()
    {
        String strBuff = "";
        String strName1 = "";
        String strName2 = "";
        String strName3 = "";

        strName1 = tfName1.getText();
        strName2 = tfName2.getText();
        strName3 = tfName3.getText();

        String strWhereClause = " WHERE 1=1 ";

        if(strName1.length() > 0)
        {
            strBuff = String.format(" and upper(p.name1) like upper('%s%%') ", strName1);
            strWhereClause += strBuff;
        }

        if(strName2.length() > 0)
        {
            strBuff = String.format(" and upper(p.name2) like upper('%s%%') ", strName2);
            strWhereClause += strBuff;
        }

        if(strName3.length() > 0)
        {
            strBuff = String.format(" and upper(p.name3) like upper('%s%%') ", strName3);
            strWhereClause += strBuff;
        }

        m_Contacts.Load(m_Connection, strWhereClause);
        m_Contacts.FillTableModel(m_TableModel);
    }
}
