package view.admin;

import Common.Con;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class RevenueManage extends JPanel {
    private JPanel pnlAbove, pnlSearch;
    private JTable revenueTable;
    private JScrollPane pnlTable;
    private JPanel pnlRevenue;
    private JComboBox<String> dateCombo;
    private JButton confirmBtn;
    private JPanel pnlBelow;
    private JTable listRoomTb;
    private JPanel pnlOption;
    private JDateChooser dateChooserSingle;
    private JDateChooser dateChooserStart;
    private JDateChooser dateChooserEnd;

    public RevenueManage() {
        setLayout(new BorderLayout());

        pnlRevenue = new JPanel();
        pnlRevenue.setLayout(new BorderLayout());

        pnlSearch = new JPanel();
        pnlSearch.setLayout(new FlowLayout());

        dateCombo = new JComboBox<>(new String[]{"Ngày", "Khoảng ngày"});
        confirmBtn = new JButton("Tìm kiếm");

        pnlOption = new JPanel();
        pnlOption.setLayout(new CardLayout());

        dateChooserSingle = new JDateChooser();
        dateChooserStart = new JDateChooser();
        dateChooserEnd = new JDateChooser();

        pnlOption.add(dateChooserSingle, "Ngày");
        JPanel dateRangePanel = new JPanel();
        dateRangePanel.setLayout(new FlowLayout());
        dateRangePanel.add(new JLabel("Bắt đầu:"));
        dateRangePanel.add(dateChooserStart);
        dateRangePanel.add(new JLabel("Kết thúc:"));
        dateRangePanel.add(dateChooserEnd);
        pnlOption.add(dateRangePanel, "Khoảng ngày");

        pnlSearch.add(new JLabel("Chọn:"));
        pnlSearch.add(dateCombo);
        pnlSearch.add(pnlOption);
        pnlSearch.add(confirmBtn);

        revenueTable = new JTable();
        pnlTable = new JScrollPane(revenueTable);

        pnlRevenue.add(pnlSearch, BorderLayout.NORTH);
        pnlRevenue.add(pnlTable, BorderLayout.CENTER);

        add(pnlRevenue, BorderLayout.CENTER);

        dateCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) (pnlOption.getLayout());
                cl.show(pnlOption, (String) dateCombo.getSelectedItem());
            }
        });

        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listRevenueData();
            }
        });

        listRevenueData();
    }



    private void listRevenueData() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("Ngày");
        vctHeader.add("Số hóa đơn");
        vctHeader.add("Hóa đơn thấp nhất");
        vctHeader.add("Hóa đơn cao nhất");
        vctHeader.add("Tổng tiền phòng");
        vctHeader.add("Tổng tiền dịch vụ");
        vctHeader.add("Tổng tiền tiện ích");
        vctHeader.add("Tổng doanh thu");


        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT DATE(b.created_at) AS date, " +
                    "COUNT(DISTINCT b.booking_id) AS bill_count, " +
                    "SUM(b.room_price) AS total_room_price, " +
                    "SUM(COALESCE(service_totals.total_service_price, 0)) AS total_service_price, " +
                    "SUM(COALESCE(amenity_totals.total_amenity_price, 0)) AS total_amenity_price, " +
                    "SUM(b.room_price + COALESCE(service_totals.total_service_price, 0) + COALESCE(amenity_totals.total_amenity_price, 0)) AS total_revenue, " +
                    "MIN(b.room_price + COALESCE(service_totals.total_service_price, 0) + COALESCE(amenity_totals.total_amenity_price, 0)) AS min_invoice, " +
                    "MAX(b.room_price + COALESCE(service_totals.total_service_price, 0) + COALESCE(amenity_totals.total_amenity_price, 0)) AS max_invoice " +
                    "FROM bookings b " +
                    "LEFT JOIN ( " +
                    "    SELECT bs.booking_id, SUM(s.service_price) AS total_service_price " +
                    "    FROM booking_services bs " +
                    "    JOIN services s ON bs.service_id = s.service_id " +
                    "    GROUP BY bs.booking_id " +
                    ") AS service_totals ON b.booking_id = service_totals.booking_id " +
                    "LEFT JOIN ( " +
                    "    SELECT ba.booking_id, SUM(a.price * ba.quantity) AS total_amenity_price " +
                    "    FROM booking_amenities ba " +
                    "    JOIN amenities a ON ba.amenity_id = a.amenity_id " +
                    "    GROUP BY ba.booking_id " +
                    ") AS amenity_totals ON b.booking_id = amenity_totals.booking_id " ;

            if (dateCombo.getSelectedItem().equals("Ngày") && dateChooserSingle.getDate() != null) {
                sql += "AND DATE(b.created_at) = ? ";
            } else if (dateCombo.getSelectedItem().equals("Khoảng ngày") && dateChooserStart.getDate() != null && dateChooserEnd.getDate() != null) {
                sql += "AND DATE(b.created_at) BETWEEN ? AND ? ";
            }

            sql += "GROUP BY DATE(b.created_at)";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sql);

            int paramIndex = 1;
            if (dateCombo.getSelectedItem().equals("Ngày") && dateChooserSingle.getDate() != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(dateChooserSingle.getDate().getTime()));
            } else if (dateCombo.getSelectedItem().equals("Khoảng ngày") && dateChooserStart.getDate() != null && dateChooserEnd.getDate() != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(dateChooserStart.getDate().getTime()));
                pstmt.setDate(paramIndex++, new java.sql.Date(dateChooserEnd.getDate().getTime()));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> vctRow = new Vector<>();
                vctRow.add(rs.getDate("date"));
                vctRow.add(rs.getInt("bill_count"));
                vctRow.add(rs.getDouble("min_invoice"));
                vctRow.add(rs.getDouble("max_invoice"));
                vctRow.add(rs.getDouble("total_room_price"));
                vctRow.add(rs.getDouble("total_service_price"));
                vctRow.add(rs.getDouble("total_amenity_price"));
                vctRow.add(rs.getDouble("total_revenue"));

                vctData.add(vctRow);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(vctData, vctHeader);
        revenueTable.setModel(model);
    }














}
