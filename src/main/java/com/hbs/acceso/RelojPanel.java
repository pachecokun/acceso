/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hbs.acceso;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Calendar;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author David Pacheco
 */
public class RelojPanel extends JPanel{
    Image reloj=null;
    float segundos=0;
    float minutos=0;
    float horas=0;
    int grueso=10;
    
    public RelojPanel(){
        setBackground(Color.white);
        setOpaque(true);
        reloj=new ImageIcon(getClass().getResource("clock.png")).getImage();
        new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setHour(Calendar.getInstance());
            }
        }).start();
    }
    
    public int mayor (){
        return Math.max(getWidth(),getHeight());
    }
    
    public int menor(){
        return Math.min(getWidth(),getHeight());
    }
    
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d=(Graphics2D)g;
        
        if(getWidth()>getHeight()){
            g2d.translate((mayor()-menor())/2, 0);
        }else{
            g2d.translate(0,(mayor()-menor())/2);
        }
        g2d.scale((float)menor()/500f,(float)menor()/500f);
        
        
        g2d.drawImage(reloj, 0,0,500,500, null);
        
        g2d.translate(245,245);
        AffineTransform at=g2d.getTransform();
        
        g2d.setColor(Color.red);
        g2d.rotate((segundos*2*Math.PI)/60f);
        g2d.rotate(Math.PI);
        g2d.fillRoundRect(-grueso/2, -grueso/2, grueso, 190, grueso, grueso);
        
        
        g2d.setTransform(at);
        g2d.setColor(Color.BLACK);
        g2d.rotate((minutos*2*Math.PI)/60f);
        g2d.rotate(Math.PI);
        g2d.fillRoundRect(-grueso/2, -grueso/2, grueso, 190, grueso, grueso);
        
        g2d.setTransform(at);
        g2d.rotate((horas*2*Math.PI)/12f);
        g2d.rotate(Math.PI);
        g2d.fillRoundRect(-grueso/2, -grueso/2, grueso, 100, grueso, grueso);
    }
    
    public void setHour(Calendar h){
            segundos=h.get(Calendar.SECOND);
            minutos=h.get(Calendar.MINUTE);
            horas=h.get(Calendar.HOUR_OF_DAY);
            repaint();
    }
}
