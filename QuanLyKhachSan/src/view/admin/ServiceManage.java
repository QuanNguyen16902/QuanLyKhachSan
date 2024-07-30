package view.admin;

import Common.Con;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

public class ServiceManage extends JPanel{
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
    private JTextArea descriptArea;
    private JComboBox statusBox;
    private JPanel pnlBelow;
    private JScrollPane pnlTable;
    private JTable listServiceTb;
    private JPanel pnlService;
    private JTextField txtProvider;

    public ServiceManage(){
        setLayout(new BorderLayout());
        add(pnlService);
        txtId.setEditable(false);
        String[] statusOption = {"Có sẵn", "Không có sẵn"};
        Arrays.stream(statusOption).forEach(statusBox::addItem);


        listServiceTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()){
                    int selectedRow = listServiceTb.getSelectedRow();
                    if(selectedRow != - 1){
                        Integer id = (Integer) listServiceTb.getValueAt(selectedRow, 0);
                        String name = (String) listServiceTb.getValueAt(selectedRow, 1);
                        String price = (String) listServiceTb.getValueAt(selectedRow, 2);
                        String provider = (String) listServiceTb.getValueAt(selectedRow, 3);
                        String available = (String) listServiceTb.getValueAt(selectedRow, 4);
                        String description = (String) listServiceTb.getValueAt(selectedRow, 5);

                        txtId.setText(String.valueOf(id));
                        txtName.setText(name);
                        txtPrice.setText(price);
                        txtProvider.setText(provider);
                        statusBox.setSelectedItem(available);
                        descriptArea.setText(description);
                    }


                }
            }
        });

        listBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadServiceData();
            }
        });
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addService();
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteService();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editService();
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteService();
            }
        });
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchService();
            }
        });
    }
    private void searchService() {
        String searchKeyword = txtSearch.getText().trim();

        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Tên dịch vụ");
        columnNames.add("Giá");
        columnNames.add("Nhà cung cấp");
        columnNames.add("Tình trạng");
        columnNames.add("Mô tả");

        Vector<Vector<Object>> data = new Vector<>();
        Con c = new Con();
        try {
            String query = "SELECT * FROM services WHERE service_name LIKE ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(query);
            pstmt.setString(1, "%" + searchKeyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> vctRow = new Vector<>();
                vctRow.add(rs.getInt("service_id"));
                vctRow.add(rs.getString("service_name"));
                vctRow.add(rs.getString("service_price"));
                vctRow.add(rs.getString("service_provider"));
                vctRow.add(rs.getString("available"));
                vctRow.add(rs.getString("description"));
                data.add(vctRow);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            listServiceTb.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtProvider.setText("");
        statusBox.setSelectedIndex(-1);
        descriptArea.setText("");
    }
    private void loadServiceData() {
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
        listServiceTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }
    private void addService(){
        String name = txtName.getText();
        String provider = txtProvider.getText();
        String price = txtPrice.getText();
        String available = (String) statusBox.getSelectedItem();
        String description = descriptArea.getText();

        Con c = new Con();
        if (name.isEmpty() || provider.isEmpty() ||  Double.parseDouble(price) <= 0 || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin và đảm bảo thông tin hợp lệ.");
            return;
        }
        try{
            String sql1 = "SELECT COUNT(*) FROM services WHERE service_name = ?";
            PreparedStatement pstm = c.connection.prepareStatement(sql1);
            pstm.setString(1, name);
            ResultSet rs = pstm.executeQuery();
            if(rs.next() && rs.getInt(1) > 0){
                JOptionPane.showMessageDialog(this, "Dịch vụ này đã tồn tại");
                return;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        try{
            String sql = "INSERT INTO services (service_name, service_price, service_provider, available, description) VALUES (?, ?, ?, ?, ?) ";
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, price);
            pstmt.setString(3, provider);
            pstmt.setString(4, available);
            pstmt.setString(5, description);
            int record = pstmt.executeUpdate();
            if(record > 0){
                JOptionPane.showMessageDialog(null, "Thêm dịch vụ thành công");
                clearFields();
                loadServiceData();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private void editService() {
        int selectedRow = listServiceTb.getSelectedRow();
        Integer id = Integer.valueOf(txtId.getText());
        String price = txtPrice.getText();
        String provider = txtProvider.getText();
        String description = descriptArea.getText();
        String name = txtName.getText();
        String status = (String) statusBox.getSelectedItem();
        if (selectedRow != -1) {
            Con c = new Con();
            try{
                String sql = "UPDATE services SET service_name = ?, service_price = ?, service_provider = ?, available = ?, description = ? WHERE service_id = ?";
                PreparedStatement pstmt = c.connection.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setDouble(2, Double.parseDouble(price));
                pstmt.setString(3, provider);
                pstmt.setString(4, status);
                pstmt.setString(5, description);
                pstmt.setInt(6, id);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công.");
                loadServiceData();
            } catch (SQLException | NumberFormatException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Hãy chọn dịch vụ để cập nhật.");
        }
    }
    private void deleteService(){
        int selectedRow = listServiceTb.getSelectedRow();
        if (selectedRow != -1) {
            int serviceId = (Integer) listServiceTb.getValueAt(selectedRow, 0);
            Con c = new Con();
            try {
                PreparedStatement pstmt = c.statement.getConnection().prepareStatement("DELETE FROM services WHERE service_id = ?");
                pstmt.setInt(1, serviceId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công.");
                loadServiceData();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Hãy chọn dịch vụ để xóa.");
        }
    }
}
