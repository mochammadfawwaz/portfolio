/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.fawwaz;

import database.Koneksi;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Asus
 */
public class MenuBayar extends javax.swing.JFrame {
    
    ArrayList listNamaBarang = new ArrayList<String>();
    ArrayList listJumlahBarang = new ArrayList<String>();
    ArrayList listHargaBarang = new ArrayList<String>();
    ArrayList listSubtotal = new ArrayList<Integer>();
    
    Koneksi connection;
    ResultSet resultSet;
    
    /**
     * Creates new form MenuBayar
     */
    public MenuBayar() {
        connection = new Koneksi();
        initComponents();
        getTotalBiayaPembelian();
    }
    
    private void getTotalBiayaPembelian(){
        String totalBiaya = String.valueOf(Transaksi.getTotalHarga());
        tvTotalBiaya.setText(totalBiaya);
    }
   
     public PageFormat getPageFormat(PrinterJob printerJob){
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = pageFormat.getPaper();
        
        double middleHeight = 9 + listJumlahBarang.size();
        double headerHeight = 1.0;
        double footerHeight = 5.0;
        double width = convert_CM_TO_PPI(7);
        double height = 
                convert_CM_TO_PPI(headerHeight + middleHeight + footerHeight);
        paper.setSize(width, height);
        paper.setImageableArea(
                0,
                10,
                width,
                height - convert_CM_TO_PPI(1)
        );
        
        pageFormat.setOrientation(PageFormat.PORTRAIT); 
        pageFormat.setPaper(paper);
        
        return pageFormat;
    }
    
    protected static double convert_CM_TO_PPI(double cm){
        return TOPPI(cm * 0.393600787);
    }
    
    protected static double TOPPI(double inch){
        return inch * 72d;
    }
    
    private int getDiskon(){
        int diskon = 0;
        String getNilaiDiskon = tfDiskon.getText();
        
        if(getNilaiDiskon.isEmpty()){
            diskon = 0;
        }else{
            String[] splitNilaiDiskon = getNilaiDiskon.split("%");
            String nilaiDiskon = splitNilaiDiskon[0];
            
            diskon = Integer.parseInt(nilaiDiskon);
        }
        
        
        return diskon;
    }
    
    private String getNamaProdukPembelian(){
        String namaProduk = "";
        ArrayList tableNamaProduk = new ArrayList<String>();
        ArrayList tableJumlahPembelian = new ArrayList<String>();
        
        String query = "SELECT nama_produk, jumlah FROM transaksi_sementara";
        resultSet = connection.eksekusiQuery(query);
        
        int jumlahKolom = 0;
        try{
            
            while(resultSet.next()){
              jumlahKolom++;
              tableNamaProduk.add(resultSet.getString(1));
              tableJumlahPembelian.add(resultSet.getString(2));
            }
            connection.closeDatabase();
        }catch(SQLException e){
            System.out.println("Error try to get value from database : " 
                    + e.getMessage());
        }
        
        for(int i = 0; i < jumlahKolom; i++){
            namaProduk += 
                    tableNamaProduk.get(i) +
                    "(" + tableJumlahPembelian.get(i) + ")";
            if(i < jumlahKolom - 1){
                namaProduk+= ",";
            }
        }
        return namaProduk;
    }
    
    private int getHargaSetelahDiskon(){
        int hargaAwal = Integer.parseInt(tvTotalBiaya.getText());
        int diskonPembelian = getDiskon();
        int tunai = hargaAwal - (hargaAwal * (diskonPembelian / 100));

        return tunai;
    }
    
    private void insertValueToTableLaporanTransaksi(){
        
        Date date = (Date)this.dsTangggal.getDate();
        String tanggal = new java.sql.Date(date.getTime()).toString();
        String namaProduk = getNamaProdukPembelian();
        String diskon = tfDiskon.getText();
        String totalBayar = String.valueOf(getHargaSetelahDiskon());
        String jumlahUang  = tfTunai.getText();
        
        String tableName = "laporan_transaksi";
        String[] colomn = {
            "tanggal",
            "nama_produk",
            "diskon",
            "total_bayar",
            "jumlah_uang"
        };
        
        String[] value = {
            tanggal,
            namaProduk,
            diskon,
            totalBayar,
            jumlahUang
        };
        
        connection.queryInsert(tableName, colomn, value);
        connection.closeDatabase();
    }
    
    private void getValueForBillTransaksi(){
        String query = "SELECT nama_produk, jumlah, harga_produk "
                + "FROM transaksi_sementara";

        resultSet = connection.eksekusiQuery(query);

        try{

            while(resultSet.next()){
                listNamaBarang.add(resultSet.getString(1));
                listJumlahBarang.add(resultSet.getString(2));
                listHargaBarang.add(resultSet.getString(3));
            }

            connection.closeDatabase();
        }catch(SQLException e){
            System.out.println("Error try to get value from tabel "
                    + "transaksi sementara(getValueForBillTransaksi) : "
                    + e.getMessage());
        }
    }

    private void getSubtotalHargaBarang(){
        for(int i = 0; i < listNamaBarang.size(); i++){
            String nilaiJumlahBarang = String.valueOf(listJumlahBarang.get(i));
            String nilaiHargaBarang = String.valueOf(listHargaBarang.get(i));

            int jumlahBarang = Integer.parseInt(nilaiJumlahBarang);
            int hargaBarang = Integer.parseInt(nilaiHargaBarang);

            int subtotal = jumlahBarang * hargaBarang;

            listSubtotal.add(subtotal);

        }
    }

    private String getDateNow(){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date = new Date();
        return format.format(date);
    }
    
    private void getDeleteValueInTableTransaksiSementara(){
        String query = "TRUNCATE TABLE `transaksi_sementara`";
        connection.eksekusiUpdate(query);
        connection.closeDatabase();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        dsTangggal = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        tfDiskon = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfTunai = new javax.swing.JTextField();
        btnBayar = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tvTotalBiaya = new javax.swing.JLabel();
        tvKembalian = new javax.swing.JLabel();

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Poppins SemiBold", 1, 18)); // NOI18N
        jLabel1.setText("Bayar");

        jLabel11.setText("________________________________________________");

        jLabel2.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel2.setText("Tanggal");

        dsTangggal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel7.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel7.setText("Diskon");

        tfDiskon.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(7, 29, 88), 2));
        tfDiskon.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfDiskonKeyReleased(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel9.setText("Tunai");

        tfTunai.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(7, 29, 88), 2));
        tfTunai.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfTunaiKeyReleased(evt);
            }
        });

        btnBayar.setBackground(new java.awt.Color(1, 126, 250));
        btnBayar.setForeground(new java.awt.Color(255, 255, 255));
        btnBayar.setText("Bayar & Cetak");
        btnBayar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnBayar.setBorderPainted(false);
        btnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBayarActionPerformed(evt);
            }
        });

        btnClose.setBackground(new java.awt.Color(255, 36, 36));
        btnClose.setForeground(new java.awt.Color(255, 255, 255));
        btnClose.setText("Close");
        btnClose.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnClose.setBorderPainted(false);
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel6.setText("Total Bayar: ");

        jLabel10.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel10.setText("Kembalian: ");

        tvTotalBiaya.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        tvTotalBiaya.setText(" ");

        tvKembalian.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        tvKembalian.setText(" ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(btnClose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(tfTunai, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tvKembalian))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(tvTotalBiaya)))
                            .addGap(127, 127, 127)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(108, 108, 108)
                                .addComponent(jLabel1))
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(dsTangggal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tfDiskon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))))
                        .addGap(25, 25, 25))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dsTangggal, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfDiskon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfTunai, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tvTotalBiaya))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(tvKembalian))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(68, Short.MAX_VALUE))
        );

        btnClose.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void tfDiskonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfDiskonKeyReleased
        
       
    }//GEN-LAST:event_tfDiskonKeyReleased

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        Transaksi.setDisplayMenuBayar(0);
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBayarActionPerformed
        Transaksi transaksi = new Transaksi();
        
        getValueForBillTransaksi();
        getSubtotalHargaBarang();
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new PrintStruk(), getPageFormat(printerJob));
        
        try{
            printerJob.print();
        }catch(PrinterException e){
            System.out.println("Error while to print bill : " + e.getMessage());
        }

        insertValueToTableLaporanTransaksi();
        getDeleteValueInTableTransaksiSementara();
        transaksi.getTable();
        
        Transaksi.setDisplayMenuBayar(0);
        this.dispose();
        
    }//GEN-LAST:event_btnBayarActionPerformed

    private void tfTunaiKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTunaiKeyReleased
        
        
        String tvTotaHarga = tvTotalBiaya.getText();
        String cashPembeliValue = tfTunai.getText();
        int totalBiaya = Integer.parseInt(tvTotaHarga);
        int Tunai = Integer.parseInt(cashPembeliValue);
        int diskon = getDiskon();
        
        int uangKembalian = 
                Tunai - (totalBiaya - (totalBiaya * diskon / 100));
        String nilaiUangkembalian = String.valueOf(uangKembalian);
        
        tvKembalian.setText(nilaiUangkembalian);
    }//GEN-LAST:event_tfTunaiKeyReleased
    
        public class PrintStruk implements Printable{

        @Override
        public int print(
                Graphics graphics,
                PageFormat pageFormat,
                int pageIndex
        ) throws PrinterException {
            int result = NO_SUCH_PAGE;
            if(pageIndex == 0){
                
                Graphics2D graphics2d = (Graphics2D) graphics;
                double width = pageFormat.getImageableWidth();
                graphics2d.translate(
                        (int)pageFormat.getImageableX(),
                        (int)pageFormat.getImageableY()
                );
                
                FontMetrics metrics = graphics2d.getFontMetrics(
                        new Font("Arial",Font.BOLD,7));
                
                int idLength = metrics.stringWidth("000");
                int amtLength = metrics.stringWidth("000000");
                int qtyLength = metrics.stringWidth("00000");
                int priceLength = metrics.stringWidth("000000");
                int prodLength = 
                        (int)width - idLength 
                        - amtLength - qtyLength - priceLength-17;
                
                try{
                    
                    int y = 20;
                    int yShift = 15;
                    int headerRectangleHeight = 15;
                    int headerRectangleHeighta = 40;
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString("    Nasythrift/Berikado",12,y);
                    y+=yShift;
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString("Situ Sukarame, Parakansalak, Parakan Salak",12,y);
                    y+=yShift;
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString(" Kab. Sukabumi, Jawa Barat",12,y);
                    y+=yShift;
                    graphics2d.drawString("   Telp. 0812-9602-9477", 12, y);
                    y+=yShift;
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString("---------------------------",12,y);
                    y+=headerRectangleHeight;
                    graphics2d.setFont(new Font("Monospaced", Font.PLAIN, 7));
                    graphics2d.drawString("Faktur  : 11111",12,y);
                    y+=yShift;
                    graphics2d.drawString("Tanggal : " + getDateNow() + " WIB", 12, y);
                    y+=yShift;
                    graphics2d.drawString("Kasir   : 001/Agung Subakti", 12, y);
                    y+=yShift;
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString("---------------------------",12,y);
                    y+=headerRectangleHeight;
                    graphics2d.setFont(new Font("Monospaced", Font.PLAIN, 7));
                    int no = 1;
                    //barang yang akan di beli
                    for(int i = 0; i < listNamaBarang.size(); i++){
                        graphics2d.drawString(no++ +". " + listNamaBarang.get(i), 1, y);
                        y+=yShift;
                        graphics2d.drawString("    " + listJumlahBarang.get(i) 
                                + " X " + listHargaBarang.get(i) + "      "
                                        + "    = " + listSubtotal.get(i), 1,y);
                        y+=yShift;
                    }
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString("---------------------------"
                            ,12,y);
                    y+=headerRectangleHeight;
                    graphics2d.setFont(new Font("Monospaced", Font.PLAIN, 7));
                    graphics2d.drawString("Sub Total       : " 
                            + tvTotalBiaya.getText(), 12, y);
                    y+=yShift;
                    graphics2d.drawString("Diskon          : " 
                            + tfDiskon.getText(), 12, y);
                    y+=yShift;
                    graphics2d.drawString("Grand Total     : " 
                            + getHargaSetelahDiskon(), 12, y);
                    y+=headerRectangleHeight;
                    graphics2d.setFont(new Font("Monospaced", Font.BOLD, 7));
                    graphics2d.drawString("---------------------------"
                            ,12,y);
                    y+=headerRectangleHeight;
                    graphics2d.drawString("** Nasythrift/Berikado **", 12, y);
                    y+=headerRectangleHeight;
                    graphics2d.setFont(new Font("Monospaced", Font.PLAIN, 7));
                    graphics2d.drawString("       TERIMA KASIH", 12, y);
                    y+=yShift;
                    graphics2d.drawString("TELAH MELAKUKAN PEMBELIAN", 12, y);
                    y+=yShift;
                    graphics2d.drawString("   INFO PEMESANAN ONLINE", 12, y);
                    y+=yShift;
                    graphics2d.drawString("INSTAGRAM: @berikado_ @nasythrif", 12, y);
                    
                }catch(Exception e){
                    System.out.println("Error generate graphics2d "
                            + "class printStruck : " + e.getMessage());
                }
                
                result = PAGE_EXISTS;
            }
        return result;
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MenuBayar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MenuBayar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MenuBayar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MenuBayar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MenuBayar().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBayar;
    private javax.swing.JButton btnClose;
    private com.toedter.calendar.JDateChooser dsTangggal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField tfDiskon;
    private javax.swing.JTextField tfTunai;
    private javax.swing.JLabel tvKembalian;
    private javax.swing.JLabel tvTotalBiaya;
    // End of variables declaration//GEN-END:variables
}
