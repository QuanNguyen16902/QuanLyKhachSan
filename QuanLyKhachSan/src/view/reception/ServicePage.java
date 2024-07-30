package view.reception;

import Common.Con;
import view.admin.AmenityOfBooking;
import view.admin.ServiceOfBooking;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

public class ServicePage extends JPanel{
    private JTabbedPane serviceAmenityPane;
    private JPanel pnlContent;
    private JPanel pnlService;
    private JPanel pnlAmenity;
    private JScrollPane pnlTable;
    private JTable serviceListTb;
    private JButton addServiceBtn;
    private JButton addAmenityBtn;
    private JTable amenityListTb;
    private JPanel pnlBooking;
    private JTable bookingListTb;
    private JButton serviceBtn;
    private JButton amenityBtn;
    private int selectedBookingId;  // Thêm trường này để lưu trữ id của booking đã chọn

    public ServicePage() {
        setLayout(new BorderLayout());
        add(pnlContent);
        loadBookings();
        loadServices();
        loadAmenities();


        serviceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedBookingId != 0){
                    new ServiceOfBooking(selectedBookingId);
                }else {
                    JOptionPane.showMessageDialog(null,"Bạn chưa chọn booking");
                }
            }
        });
        amenityBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedBookingId != 0){
                    new AmenityOfBooking(selectedBookingId);
                }else {
                    JOptionPane.showMessageDialog(null,"Bạn chưa chọn booking");
                }
                loadBookings();
            }
        });
        bookingListTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = bookingListTb.getSelectedRow();
                    if (selectedRow != -1) { // Nếu có dòng được chọn
                        selectedBookingId = (int) bookingListTb.getValueAt(selectedRow, 0); // Lấy id của booking đã chọn
                        String customerName = (String) bookingListTb.getValueAt(selectedRow, 1);
                        String roomNum = (String) bookingListTb.getValueAt(selectedRow, 2);
                        String roomType = (String) bookingListTb.getValueAt(selectedRow, 3);
                        String roomPrice = (String) bookingListTb.getValueAt(selectedRow, 4);
                        String bookingStatus = (String) bookingListTb.getValueAt(selectedRow, 5);

                        Timestamp checkInDate = (Timestamp) bookingListTb.getValueAt(selectedRow, 6);
                        Timestamp checkOutDate = (Timestamp) bookingListTb.getValueAt(selectedRow, 7);
                        Double totalInvoice = (Double) bookingListTb.getValueAt(selectedRow, 8);

                    }
                }
            }
        });
    }
    private void loadBookings() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("ID");  // Thêm ID vào tiêu đề
        vctHeader.add("Người đặt phòng");
        vctHeader.add("Số phòng");
        vctHeader.add("Loại phòng");
        vctHeader.add("Giá phòng");
        vctHeader.add("Trạng thái");
        vctHeader.add("Ngày nhận phòng");
        vctHeader.add("Ngày trả phòng");
        vctHeader.add("Tổng tiền");

        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT * FROM bookings WHERE booking_status NOT IN ('Hủy đặt phòng', 'Không đến')";
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> vctRow = new Vector<>();
                vctRow.add(rs.getInt("booking_id"));  // Thêm id vào dữ liệu hàng
                vctRow.add(rs.getString("customer_name"));
                vctRow.add(rs.getString("room_num"));
                vctRow.add(rs.getString("room_type"));
                vctRow.add(rs.getString("room_price"));
                vctRow.add(rs.getString("booking_status"));
                vctRow.add(rs.getTimestamp("check_in_date"));
                vctRow.add(rs.getTimestamp("check_out_date"));
                vctRow.add(rs.getDouble("total_invoice"));
                vctData.add(vctRow);
            }
            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(vctData, vctHeader);
        bookingListTb.setModel(model);
    }
    private void loadServices() {
        Vector vctHeader= new Vector<>();
        vctHeader.add("ID");
        vctHeader.add("Tên dịch vụ");
        vctHeader.add("Giá");
        vctHeader.add("Nhà cung cấp");
        vctHeader.add("Tình trạng");
        vctHeader.add("Mô tả");
        Vector vctData = new Vector<>();

        Con c = new Con();
        try{
            String sql = "SELECT * FROM services";
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()){
                Vector vctRow = new Vector();
                vctRow.add(rs.getInt("service_id"));
                vctRow.add(rs.getString("service_name"));
                vctRow.add(rs.getString("service_price"));
                vctRow.add(rs.getString("service_provider"));
                vctRow.add(rs.getString("available"));
                vctRow.add(rs.getString("description"));
                vctData.add(vctRow);
            }
            rs.close();

        }catch (SQLException e){
            e.printStackTrace();
        }
        serviceListTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }
    private void loadAmenities() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Amenity ID");
        columnNames.add("Name");
        columnNames.add("Quantity");
        columnNames.add("Price");
        columnNames.add("Available");
        columnNames.add("Notes");

        Vector<Vector<Object>> data = new Vector<>();
        Con c = new Con();
        try
        {

            ResultSet rs = c.statement.executeQuery("SELECT * FROM amenities");
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("amenity_id"));
                row.add(rs.getString("amenity_name"));
                row.add(rs.getInt("quantity"));
                row.add(rs.getDouble("price"));
                row.add(rs.getBoolean("available"));
                row.add(rs.getString("notes"));
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            amenityListTb.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
