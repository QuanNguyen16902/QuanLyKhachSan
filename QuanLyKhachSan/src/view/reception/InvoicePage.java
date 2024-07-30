package view.reception;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class InvoicePage extends JPanel{
    private JPanel pnlInvoice;
    private JScrollPane pnlTable;
    private JTable invoiceListTb;
    public InvoicePage(){
        setLayout(new BorderLayout());
        add(pnlInvoice);
        listInvoice();
    }
    private void listInvoice() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("ID");
        vctHeader.add("ID Khách hàng");
        vctHeader.add("Ngày tạo hóa đơn");
        vctHeader.add("Số người");
        vctHeader.add("Số ngày ở");
        vctHeader.add("Giá phòng");
        vctHeader.add("Dịch vụ");
        vctHeader.add("Tiện nghi");
        vctHeader.add("Tổng tiền");

        Vector<Vector<Object>> vctData = new Vector<>();
        Con c = new Con();
        String sql = "SELECT * FROM invoices";
        try {
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("invoice_id"));
                row.add(rs.getInt("customer_id"));
                row.add(rs.getDate("invoice_date"));
                row.add(rs.getInt("num_of_people"));
                row.add(rs.getInt("num_of_day"));
                row.add(rs.getDouble("room_price"));
                row.add(rs.getDouble("total_service"));
                row.add(rs.getDouble("total_amenity"));
                row.add(rs.getDouble("total_amount"));
                vctData.add(row);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set dữ liệu vào table model
        invoiceListTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }
}
