/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hbs.acceso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pacheco
 */
public class DB {
    private static Connection conn = null;
    
    public static Connection getConexion(){
        if(conn == null){
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                conn =
                   DriverManager.getConnection("jdbc:mysql://localhost/acceso?" +
                                               "user=root&password=root");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return conn;
    }
    
    public static int ejecutarUpdate(String query) throws SQLException{
        System.out.println(query);
        Statement stmt = getConexion().createStatement();
        return stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
    }
    
    public static ResultSet ejecutarConsulta(String query) throws SQLException{
        System.out.println(query);
        Statement stmt = getConexion().createStatement();
        return stmt.executeQuery(query);
    }
    
    public static List<Empleado> getEmpleados() throws SQLException{
        List<Empleado> r = new ArrayList<>();
        ResultSet rs = ejecutarConsulta("select*from empleado");
        while(rs.next()){
            Empleado e = new Empleado();
            e.setNombre(rs.getString("nombre"));
            e.setAp(rs.getString("apPaterno"));
            e.setAp(rs.getString("apMaterno"));
            e.setId(rs.getInt("idEmpleado"));
            e.setHuella(rs.getBytes("fingerprint"));
            r.add(e);
        }
        return r;
    }
    
}
