package com.bookstore;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BookListServlet extends HttpServlet {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookstore_db";
    private static final String DB_USER = "root";       
    private static final String DB_PASSWORD = ""; // UPDATE THIS

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("formAction");
        int id = Integer.parseInt(request.getParameter("bookId"));
        String title = request.getParameter("bookTitle");
        String author = request.getParameter("bookAuthor");
        double price = Double.parseDouble(request.getParameter("bookPrice"));
        int qty = Integer.parseInt(request.getParameter("quantity"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                
                if ("update".equals(action)) {
                    String sql = "UPDATE ebookshop SET book_title=?, book_author=?, book_price=?, quantity=? WHERE book_id=?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, title); pstmt.setString(2, author);
                        pstmt.setDouble(3, price); pstmt.setInt(4, qty); pstmt.setInt(5, id);
                        pstmt.executeUpdate(); 
                    }
                } else {
                    String sql = "INSERT INTO ebookshop (book_id, book_title, book_author, book_price, quantity) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, id); pstmt.setString(2, title); pstmt.setString(3, author);
                        pstmt.setDouble(4, price); pstmt.setInt(5, qty);
                        pstmt.executeUpdate(); 
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        response.sendRedirect("books");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String urlAction = request.getParameter("action");

        // DELETE Logic
        if ("delete".equals(urlAction)) {
            int id = Integer.parseInt(request.getParameter("id"));
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "DELETE FROM ebookshop WHERE book_id=?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, id); pstmt.executeUpdate();
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            response.sendRedirect("books");
            return;
        }

        // Form Variables
        String editId = ""; String editTitle = ""; String editAuthor = "";
        String editPrice = ""; String editQty = ""; 
        String formActionValue = "add"; 
        String buttonText = "<i class='fas fa-plus'></i> Add New Book";
        String readonlyId = "";

        // EDIT Logic (Fill form)
        if ("edit".equals(urlAction)) {
            int id = Integer.parseInt(request.getParameter("id"));
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "SELECT * FROM ebookshop WHERE book_id=?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, id);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                editId = String.valueOf(rs.getInt("book_id"));
                                editTitle = rs.getString("book_title");
                                editAuthor = rs.getString("book_author");
                                editPrice = String.valueOf(rs.getDouble("book_price"));
                                editQty = String.valueOf(rs.getInt("quantity"));
                                formActionValue = "update";
                                buttonText = "<i class='fas fa-save'></i> Update Book";
                                readonlyId = "readonly style='background-color:#f4f7fe; cursor:not-allowed;'";
                            }
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // --- RENDER HTML ---
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><title>Bookstore Dashboard</title>");
        out.println("<link rel='stylesheet' type='text/css' href='styles.css'>");
        // FontAwesome for modern icons!
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css'>");
        out.println("</head><body>");
        
        // Header
        out.println("<header class='dashboard-header'><h1>📚 Bookstore <b>Admin</b></h1></header>");

        // Main Grid Layout
        out.println("<div class='main-wrapper'>");

        // COLUMN 1: The Form Card
        out.println("<div class='card'>");
        out.println("<h2 class='card-title'>" + ("update".equals(formActionValue) ? "Edit Book" : "Add New Book") + "</h2>");
        out.println("<form action='books' method='POST'>");
        out.println("<input type='hidden' name='formAction' value='" + formActionValue + "'>");
        
        out.println("<div class='input-group'><label>Book ID</label>");
        out.println("<input type='number' name='bookId' value='" + editId + "' required " + readonlyId + "></div>");
        
        out.println("<div class='input-group'><label>Title</label>");
        out.println("<input type='text' name='bookTitle' value='" + editTitle + "' required></div>");
        
        out.println("<div class='input-group'><label>Author</label>");
        out.println("<input type='text' name='bookAuthor' value='" + editAuthor + "' required></div>");
        
        out.println("<div class='input-group'><label>Price (₹)</label>");
        out.println("<input type='number' step='0.01' name='bookPrice' value='" + editPrice + "' required></div>");
        
        out.println("<div class='input-group'><label>Stock Quantity</label>");
        out.println("<input type='number' name='quantity' value='" + editQty + "' required></div>");
        
        out.println("<button type='submit' class='btn-submit'>" + buttonText + "</button>");
        
        if ("update".equals(formActionValue)) {
            out.println("<a href='books' class='btn-cancel'>Cancel Edit</a>");
        }
        out.println("</form></div>");

        // COLUMN 2: The Table Card
        out.println("<div class='card' style='overflow:hidden;'>");
        out.println("<h2 class='card-title'>Current Inventory</h2>");
        out.println("<div class='table-container'><table><thead><tr>");
        out.println("<th>ID</th><th>Title</th><th>Author</th><th>Price</th><th>Stock</th><th>Actions</th>");
        out.println("</tr></thead><tbody>");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM ebookshop")) {

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td><b>#" + rs.getInt("book_id") + "</b></td>");
                    out.println("<td>" + rs.getString("book_title") + "</td>");
                    out.println("<td>" + rs.getString("book_author") + "</td>");
                    out.println("<td><span class='price-pill'>₹" + rs.getString("book_price") + "</span></td>");
                    out.println("<td><span class='stock-pill'>" + rs.getInt("quantity") + "</span></td>");
                    
                    out.println("<td>");
                    out.println("<a href='books?action=edit&id=" + rs.getInt("book_id") + "' class='btn-action btn-edit'><i class='fas fa-pen' style='margin-right:5px;'></i> Edit</a>");
                    out.println("<a href='books?action=delete&id=" + rs.getInt("book_id") + "' class='btn-action btn-delete' onclick=\"return confirm('Delete this book forever?');\"><i class='fas fa-trash' style='margin-right:5px;'></i> Delete</a>");
                    out.println("</td>");
                    out.println("</tr>");
                }
            }
        } catch (Exception e) {
            out.println("<tr><td colspan='6' style='color:red;'><b>Database Error:</b> " + e.getMessage() + "</td></tr>");
        }

        out.println("</tbody></table></div></div>");
        out.println("</div></body></html>");
    }
}