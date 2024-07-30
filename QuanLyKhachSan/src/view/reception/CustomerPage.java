package view.reception;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class CustomerPage extends JPanel{
    private JPanel pnlCustomer;
    private JScrollPane pnlTable;
    private JTable customerListTb;
    public CustomerPage(){
        setLayout(new BorderLayout());
        add(pnlCustomer);
        listCustomer();
    }
    private void listCustomer() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("Tên");
        vctHeader.add("Địa chỉ");
        vctHeader.add("SĐT");
        vctHeader.add("Email");
        vctHeader.add("CCCD");
        vctHeader.add("Giới tính");
        vctHeader.add("Tình trạng");
        vctHeader.add("Ngày đăng ký");

        Vector<Vector<Object>> vctData = new Vector<>();
        Con c = new Con();
        String sql = "SELECT * FROM customers WHERE account_status = 'Active'";
        try {
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("full_name"));
                row.add(rs.getString("address"));
                row.add(rs.getString("phone_number"));
                row.add(rs.getString("email"));
                row.add(rs.getString("id_card_number"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("account_status"));
                row.add(rs.getTimestamp("registration_date"));
                vctData.add(row);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set dữ liệu vào table model
        customerListTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }
}
