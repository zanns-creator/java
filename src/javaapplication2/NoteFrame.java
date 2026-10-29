package javaapplication2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.time.LocalDate;

public class NoteFrame extends JFrame {
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JTable tableNotes;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    // Konfigurasi Database MySQL
    private final String DB_URL = "jdbc:mysql://localhost:3306/db_noteme";
    private final String DB_USER = "root";
    private final String DB_PASS = ""; // Sesuaikan jika MySQL kamu pakai password

    // Palet Warna Flat UI
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);   
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);    
    private final Color DANGER_COLOR = new Color(231, 76, 60);     
    private final Color WARNING_COLOR = new Color(243, 156, 18);   
    private final Color BG_COLOR = new Color(245, 247, 250);       

    public NoteFrame() {
        setTitle("NoteMe - Database Connected");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_COLOR);
        add(mainPanel);

        // --- PANEL KIRI (FORM INPUT) ---
        JPanel panelLeft = new JPanel();
        panelLeft.setLayout(null);
        panelLeft.setPreferredSize(new Dimension(330, 0));
        panelLeft.setBackground(Color.WHITE);
        panelLeft.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 225, 233), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel("Judul Catatan");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitle.setBounds(15, 20, 150, 20);
        panelLeft.add(lblTitle);

        txtTitle = new JTextField();
        txtTitle.setBounds(15, 45, 290, 35);
        txtTitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panelLeft.add(txtTitle);

        JLabel lblContent = new JLabel("Isi Catatan");
        lblContent.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblContent.setBounds(15, 90, 150, 20);
        panelLeft.add(lblContent);

        txtContent = new JTextArea();
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollContent = new JScrollPane(txtContent);
        scrollContent.setBounds(15, 115, 290, 160);
        panelLeft.add(scrollContent);

        btnAdd = createStyledButton("Tambah", SUCCESS_COLOR);
        btnAdd.setBounds(15, 295, 140, 38);
        panelLeft.add(btnAdd);

        btnUpdate = createStyledButton("Edit", WARNING_COLOR);
        btnUpdate.setBounds(165, 295, 140, 38);
        panelLeft.add(btnUpdate);

        btnDelete = createStyledButton("Hapus", DANGER_COLOR);
        btnDelete.setBounds(15, 345, 140, 38);
        panelLeft.add(btnDelete);

        btnClear = createStyledButton("Reset", new Color(127, 140, 141));
        btnClear.setBounds(165, 345, 140, 38);
        panelLeft.add(btnClear);

        mainPanel.add(panelLeft, BorderLayout.WEST);

        // --- PANEL KANAN (TABEL) ---
        JPanel panelRight = new JPanel(new BorderLayout());
        panelRight.setBackground(Color.WHITE);
        panelRight.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 225, 233), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTableTitle = new JLabel("Daftar Catatan Tersimpan (MySQL)");
        lblTableTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTableTitle.setForeground(PRIMARY_COLOR);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelRight.add(lblTableTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Judul", "Isi Catatan", "Tanggal"}, 0);
        tableNotes = new JTable(tableModel);
        tableNotes.setRowHeight(28);
        tableNotes.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tableNotes.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tableNotes.getTableHeader().setBackground(new Color(240, 243, 246));
        
        tableNotes.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableNotes.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollTable = new JScrollPane(tableNotes);
        panelRight.add(scrollTable, BorderLayout.CENTER);

        mainPanel.add(panelRight, BorderLayout.CENTER);

        // Load data dari database saat pertama kali dibuka
        loadDataToTable();

        // --- EVENT LISTENERS ---
        tableNotes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableNotes.getSelectedRow();
                if (row >= 0) {
                    txtTitle.setText(tableModel.getValueAt(row, 1).toString());
                    txtContent.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });

        // Tombol Tambah (INSERT)
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = txtTitle.getText().trim();
                String content = txtContent.getText().trim();
                if (!title.isEmpty() && !content.isEmpty()) {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                        String sql = "INSERT INTO notes (title, content, date) VALUES (?, ?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, title);
                        pstmt.setString(2, content);
                        pstmt.setString(3, LocalDate.now().toString());
                        pstmt.executeUpdate();
                        
                        JOptionPane.showMessageDialog(null, "Catatan berhasil ditambahkan ke database!");
                        loadDataToTable();
                        clearForm();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Gagal menyimpan: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Judul dan isi catatan tidak boleh kosong!");
                }
            }
        });

        // Tombol Edit (UPDATE)
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tableNotes.getSelectedRow();
                if (row >= 0) {
                    int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    String title = txtTitle.getText().trim();
                    String content = txtContent.getText().trim();

                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                        String sql = "UPDATE notes SET title = ?, content = ? WHERE id = ?";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, title);
                        pstmt.setString(2, content);
                        pstmt.setInt(3, id);
                        pstmt.executeUpdate();

                        JOptionPane.showMessageDialog(null, "Catatan berhasil diperbarui!");
                        loadDataToTable();
                        clearForm();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(null, "Gagal memperbarui: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Pilih baris pada tabel yang ingin di-edit!");
                }
            }
        });

        // Tombol Hapus (DELETE)
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tableNotes.getSelectedRow();
                if (row >= 0) {
                    int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    int confirm = JOptionPane.showConfirmDialog(null, "Yakin ingin menghapus catatan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                            String sql = "DELETE FROM notes WHERE id = ?";
                            PreparedStatement pstmt = conn.prepareStatement(sql);
                            pstmt.setInt(1, id);
                            pstmt.executeUpdate();

                            JOptionPane.showMessageDialog(null, "Catatan berhasil dihapus!");
                            loadDataToTable();
                            clearForm();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Gagal menghapus: " + ex.getMessage());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Pilih baris pada tabel yang ingin dihapus!");
                }
            }
        });

        btnClear.addActionListener(e -> clearForm());
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM notes")) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String date = rs.getString("date");
                
                tableModel.addRow(new Object[]{id, title, content, date});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Gagal memuat database:\n" + ex.getMessage());
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    private void clearForm() {
        txtTitle.setText("");
        txtContent.setText("");
        tableNotes.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NoteFrame().setVisible(true));
    }
}