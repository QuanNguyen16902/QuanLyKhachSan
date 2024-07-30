package view.admin;

import Common.Con;
import Common.CustomTableCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;

public class AmenityManage extends JPanel{
    private JPanel pnlAbove;
    private JPanel pnlTitle;
    private JPanel pnlField;
    private JTextField txtId;
    private JButton deleteBtn;
    private JButton listBtn;
    private JButton addBtn;
    private JButton editBtn;
    private JButton clearButton;
    private JButton searchBtn;
    private JTextField txtSearch;
    private JTextField txtName;
    private JTextField txtPrice;
    private JPanel pnlBelow;
    private JScrollPane pnlTable;
    private JTable listAmenityTb;
    private JTextArea noteArea;
    private JSpinner quantitySpin;
    private JComboBox statusBox;
    private JPanel pnlAmenity;


        public AmenityManage() {
            setLayout(new BorderLayout());
            listAmenityTb.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
            add(pnlAmenity);
            String[] statusOptions = {"Có sẵn", "Không có sẵn"};
            Arrays.stream(statusOptions).forEach(statusBox::addItem);
//
            txtId.setEditable(false);
            listAmenityTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selectedRow = listAmenityTb.getSelectedRow();
                        if (selectedRow != -1) { // Nếu có dòng được chọn
                            Integer id = (Integer) listAmenityTb.getValueAt(selectedRow, 0);
                            String name = (String) listAmenityTb.getValueAt(selectedRow, 1);
                            Integer quantity = (Integer) listAmenityTb.getValueAt(selectedRow, 2);
                            Double price = (Double) listAmenityTb.getValueAt(selectedRow, 3);
                            Boolean available = (Boolean) listAmenityTb.getValueAt(selectedRow, 4);
                            String notes = (String) listAmenityTb.getValueAt(selectedRow, 5);

                            // Hiển thị thông tin lên các JTextField
                            txtName.setText(name);
                            txtId.setText(String.valueOf(id));
                            txtPrice.setText(String.valueOf(price));
                            statusBox.setSelectedItem(available ? "Có sẵn" : "Không có sẵn");
                            quantitySpin.setValue(quantity);
                            noteArea.setText(notes);
                        }
                    }
                }
            });
            listBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadAmenities();
                }
            });
            addBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addAmenity();
                }
            });
            editBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateAmenity();
                }
            });
            deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteAmenity();
                }
            });
            searchBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchAmenity();
                }
            });
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearFields();
                }
            });
        }
    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        quantitySpin.setValue(0);
        statusBox.setSelectedIndex(-1);
        noteArea.setText("");
    }
    private void searchAmenity() {
        String searchKeyword = txtSearch.getText().trim();

        Vector<String> columnNames = new Vector<>();
        columnNames.add("Amenity ID");
        columnNames.add("Name");
        columnNames.add("Quantity");
        columnNames.add("Price");
        columnNames.add("Available");
        columnNames.add("Notes");

        Vector<Vector<Object>> data = new Vector<>();
        Con c = new Con();
        try {
            String query = "SELECT * FROM amenities WHERE amenity_name LIKE ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(query);
            pstmt.setString(1, "%" + searchKeyword + "%");
            ResultSet rs = pstmt.executeQuery();
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
            listAmenityTb.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
                listAmenityTb.setModel(model);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }


    private void addAmenity() {
        String id = txtId.getText();
        Integer quantity = (Integer) quantitySpin.getValue();
        String price = txtPrice.getText();
        String notes = noteArea.getText();
        String name = txtName.getText();
        Boolean status = "Có sẵn".equals(statusBox.getSelectedItem());

        Con c = new Con();
        if (name.isEmpty() || quantity <= 0 || price.isEmpty() || Double.parseDouble(price) <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin và đảm bảo số lượng và giá hợp lệ.");
            return;
        }

        try {
            PreparedStatement pstmt = c.connection.prepareStatement("SELECT COUNT(*) FROM amenities WHERE amenity_name = ?");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Tiện nghi với tên này đã tồn tại.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement pstmt = c.connection.prepareStatement("INSERT INTO amenities (amenity_name, quantity, price, available, notes) VALUES (?, ?, ?, ?, ?)");

            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, Double.parseDouble(price));
            pstmt.setBoolean(4, status);
            pstmt.setString(5, notes);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm tiện nghi thành công.");
            loadAmenities();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAmenity() {
            int selectedRow = listAmenityTb.getSelectedRow();
                Integer id = Integer.valueOf(txtId.getText());
                Integer quantity = (Integer) quantitySpin.getValue();
                String price = txtPrice.getText();
                String notes = noteArea.getText();
                String name = txtName.getText();
                Boolean status = "Có sẵn".equals(statusBox.getSelectedItem());
            if (selectedRow != -1) {
                Con c = new Con();
                try{
                    String sql = "UPDATE amenities SET amenity_name = ?, quantity = ?, price = ?, available = ?, notes = ? WHERE amenity_id = ?";
                    PreparedStatement pstmt = c.connection.prepareStatement(sql);
                    pstmt.setString(1, name);
                    pstmt.setInt(2, quantity);
                    pstmt.setDouble(3, Double.parseDouble(price));
                    pstmt.setBoolean(4, status);
                    pstmt.setString(5, notes);
                    pstmt.setInt(6, id);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Cập nhật tiện nghi thành công.");
                    loadAmenities();
                } catch (SQLException | NumberFormatException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hãy chọn tiện nghi để cập nhật.");
            }
        }

    private void deleteAmenity() {
        int selectedRow = listAmenityTb.getSelectedRow();
        if (selectedRow != -1) {
            int amenityId = (Integer) listAmenityTb.getValueAt(selectedRow, 0);
            Con c = new Con();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa đặt phòng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION){
                try {
                    c.connection.setAutoCommit(false);

                    PreparedStatement pstmtDeleteBookingAmenities = c.statement.getConnection().prepareStatement("DELETE FROM booking_amenities WHERE amenity_id = ?");
                    pstmtDeleteBookingAmenities.setInt(1, amenityId);
                    pstmtDeleteBookingAmenities.executeUpdate();

                    PreparedStatement pstmtDeleteAmenities = c.statement.getConnection().prepareStatement("DELETE FROM amenities WHERE amenity_id = ?");
                    pstmtDeleteAmenities.setInt(1, amenityId);
                    pstmtDeleteAmenities.executeUpdate();

                    c.connection.commit();
                    JOptionPane.showMessageDialog(this, "Xóa tiện nghi thành công.");
                    loadAmenities();
                } catch (SQLException ex) {
                    try {
                        // Rollback the transaction in case of error
                        c.connection.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    ex.printStackTrace();
                } finally {
                    try {
                        // Set auto-commit back to true
                        c.connection.setAutoCommit(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } else {
            JOptionPane.showMessageDialog(this, "Hãy chọn tiện nghi để xóa.");
        }
    }





}


