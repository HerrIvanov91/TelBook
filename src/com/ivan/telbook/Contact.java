package com.ivan.telbook;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.*;
import java.sql.*;

public class Contact {
    public int m_nID;
    public String m_strName1;
    public String m_strName2;
    public String m_strName3;
    public String m_strTel;
    public String m_strAddress;

    public Contact() {
        m_nID = 0;
        m_strName1 = "";
        m_strName2 = "";
        m_strName3 = "";
        m_strTel = "";
        m_strAddress = "";
    }

    public boolean Save(Connection sql)
    {
        if(sql == null)
            return false;

        String strQuery = "";

        if(m_nID > 0)
        {
            strQuery = String.format(
                                        "UPDATE Person \n" +
                                        "SET name1 = '%s', \n" +
                                        "name2 = '%s', \n" +
                                        "name3 = '%s', \n" +
                                        "tel = '%s', \n" +
                                        "address = '%s' \n" +
                                        "WHERE id = %d",
                                        m_strName1, m_strName2, m_strName3, m_strTel, m_strAddress,
                                        m_nID

                                    );
        }

        else
        {
            strQuery = String.format(
                                        "INSERT INTO Person (name1, name2, name3, tel, address) \n" +
                                        "VALUES ('%s', '%s', '%s', '%s', '%s') \n",
                                        m_strName1, m_strName2, m_strName3, m_strTel, m_strAddress
                                    );
        }

        Statement stmt = null;
        try {
            stmt = sql.createStatement();
            stmt.execute(strQuery);

            if(m_nID <= 0)
            {
                strQuery = "SELECT currval(pg_get_serial_sequence('Person','id'));";
                ResultSet rs = stmt.executeQuery(strQuery);
                while ( rs.next() ) {
                    m_nID = rs.getInt(1);
                }
            }
        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    public boolean DeleteItem(Connection sql)
    {
        if (sql == null)
            return false;

        String strQuery = String.format("DELETE FROM Person WHERE id = %d", m_nID);

        Statement stmt = null;
        try {
            stmt = sql.createStatement();
            stmt.execute(strQuery);
        }
        catch (Exception e){
            return false;
        }

        return true;
    }
}

class ContactContainer extends ArrayList<Contact>
{
    public ContactContainer()
    {

    }

    public boolean Save(Connection sql)
    {
        boolean bRes = true;
        Contact lpItem = null;
        for(int n=0; n<this.size(); n++)
        {
            lpItem = this.get(n);
            bRes = bRes && lpItem.Save(sql);
        }

        return bRes;
    }

    public boolean Load(Connection sql, String strWhereClause)
    {
        String strQuery = "";
        Statement stmt = null;

        strQuery = String.format("SELECT p.id, p.name1, p.name2, p.name3, p.tel, p.address FROM Person AS p %s ", strWhereClause);

        this.clear();
        try {
            stmt = sql.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);

            Contact lpItem = null;
            while (rs.next()) {
                lpItem = new Contact();
                lpItem.m_nID = rs.getInt("id");
                lpItem.m_strName1 = rs.getString("name1");
                lpItem.m_strName2 = rs.getString("name2");
                lpItem.m_strName3 = rs.getString("name3");
                lpItem.m_strTel = rs.getString("tel");
                lpItem.m_strAddress = rs.getString("address");
                this.add(lpItem);
            }
        }

        catch (Exception e){
            return false;
        }

        return true;
    }

    public void FillTableModel(DefaultTableModel tableModel)
    {
        if (tableModel.getRowCount() > 0) {
            for (int i = tableModel.getRowCount() - 1; i > -1; i--) {
                tableModel.removeRow(i);
            }
        }

        Contact lpItem = null;
        for(int n=0; n<this.size(); n++)
        {
            lpItem = this.get(n);
            tableModel.insertRow(tableModel.getRowCount(), new Object[]{lpItem.m_strName1, lpItem.m_strName2, lpItem.m_strName3, lpItem.m_strTel, lpItem.m_strAddress});
        }
    }

    public static void centreWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }
}