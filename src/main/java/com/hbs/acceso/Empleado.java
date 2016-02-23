/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hbs.acceso;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pacheco
 */
public class Empleado {
    private int id;
    private String nombre;
    private String ap;
    private String am;
    private byte[] huella;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the ap
     */
    public String getAp() {
        return ap;
    }

    /**
     * @param ap the ap to set
     */
    public void setAp(String ap) {
        this.ap = ap;
    }

    /**
     * @return the am
     */
    public String getAm() {
        return am;
    }

    /**
     * @param am the am to set
     */
    public void setAm(String am) {
        this.am = am;
    }

    /**
     * @return the huella
     */
    public byte[] getHuella() {
        return huella;
    }

    /**
     * @param huella the huella to set
     */
    public void setHuella(byte[] huella) {
        this.huella = huella;
    }
    
    public int acceso(){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
            String dia = sdf.format(new Date());
            String hora = sdf2.format(new Date());
            ResultSet rs = DB.ejecutarConsulta("SELECT*FROM dia where fecha ='"+dia+"' and idEmpleado='"+id+"'");
            
            if(rs.next()){
                int idEmpleado = id;
                int idHorario = rs.getInt("idHorario");
                if(rs.getString("entrada")==null){
                    DB.ejecutarUpdate("update dia set entrada ='"+hora+"' where idEmpleado='"+idEmpleado+"' and idHorario='"+idHorario+"' and fecha='"+dia+"'");
                    return 1;
                }
                else if(rs.getString("salidaComida")==null){
                    DB.ejecutarUpdate("update dia set salidaComida='"+hora+"' where idEmpleado='"+idEmpleado+"' and idHorario='"+idHorario+"' and fecha='"+dia+"'");
                    return 2;
                }
                else if(rs.getString("entradaComida")==null){
                    DB.ejecutarUpdate("update dia set  entradaComida='"+hora+"' where idEmpleado='"+idEmpleado+"' and idHorario='"+idHorario+"' and fecha='"+dia+"'");
                    return 3;
                }
                else if(rs.getString("salida")==null){
                    DB.ejecutarUpdate("update dia set  salida='"+hora+"' where idEmpleado='"+idEmpleado+"' and idHorario='"+idHorario+"' and fecha='"+dia+"'");
                    return 4;
                }
                else{
                    return 5;
                }
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
