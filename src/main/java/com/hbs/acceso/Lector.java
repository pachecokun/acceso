/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hbs.acceso;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Engine.Candidate;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase para enrolamiento y validación de huellas
 * @author aaraujo
 */
public class Lector {

    public enum Evento{
        DETECTAR,
        FINALIZADO,
        ERROR
    }
    
    public Reader r;
    Engine engine;
    byte[] data;
    
    /**
     * Interfaz para recibir eventos al enrolar huella
     */
    public static interface EnrolamientoListener{

        /**
         * Es activado cuando se genera
         * @param e
         */
        void eventoEnrolamiento(Evento e, String msg);
    }
    
    public void cancelarDeteccion(){
        try {
            r.CancelCapture();
            r.Close();
        } catch (Exception e) {
        }
    }
    
    /**
     * Inicializa el lector y el motor de identificación
     */
    public Lector() throws UareUException,ArrayIndexOutOfBoundsException{
        ReaderCollection lectores = UareUGlobal.GetReaderCollection();
        int i = 0;
        while(lectores.size()==0&&i++<10){
            lectores.GetReaders();
        }
        r = lectores.get(0);
        engine = UareUGlobal.GetEngine();
    }
    
    /**
     * Enrola huella, envía eventos DETECTAR, FINALIZADO y ERROR mediante el escuchador
     * @param escuchador
     * @return patron en forma de bytes
     */
    public void enrolar(final EnrolamientoListener l){
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {    
                    r.Open(Reader.Priority.EXCLUSIVE);  
                    
                    final Fmd huellas[] = new Fmd[4];
                    
                    for(int i = 0;i<4;i++){
                        l.eventoEnrolamiento(Evento.DETECTAR, "");
                        huellas[i] = engine.CreateFmd(r.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT,  500, -1).image, Fmd.Format.ANSI_378_2004);
                    }
                    
                    Fmd fmd = engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, new Engine.EnrollmentCallback() {

                        int i=0;
                        
                        @Override
                        public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
                            Engine.PreEnrollmentFmd result = new Engine.PreEnrollmentFmd();
                            try {
                                result.fmd = huellas[(i++)%4];
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return null;
                            }
                            return result;
                        }
                    });
                    data = fmd.getData();
                    l.eventoEnrolamiento(Evento.FINALIZADO,"");
                    //System.out.println(fmd.getWidth()+"x"+fmd.getHeight()+","+fmd.getResolution());

                    
                    r.Close();

                } catch (UareUException ex) {
                    cancelarDeteccion();
                    String r = ex.toString();
                    if(ex.toString().contains("FMD")){
                        l.eventoEnrolamiento(Evento.ERROR,"Las huellas no coinciden. Intente de nuevo.");
                    }
                    else{
                        l.eventoEnrolamiento(Evento.ERROR, "Lector no conectado o no disponible.");
                    }
                    ex.printStackTrace();
                }
                catch(Exception ex){
                    cancelarDeteccion();
                    l.eventoEnrolamiento(Evento.ERROR,"Error al detectar la huella. Intente de nuevo.");
                    ex.printStackTrace();
                }
            }
        }).start();
        
    }

    public byte[] getData() {
        return data;
    }
    
    
    
    /**
     * Recibe un patron a validar
     * @param pattern
     * @return Si la huella es correcta o no
     */
    public boolean validar(byte[] pattern){
        try {
            r.Open(Reader.Priority.EXCLUSIVE);
            Fmd fmd1 = UareUGlobal.GetImporter().ImportFmd(pattern, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
            Fmd fmd2 = engine.CreateFmd(r.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT,  500, -1).image, Fmd.Format.ANSI_378_2004);
                r.Close();
            return (engine.Compare(fmd2, 0, fmd1, 0)<2000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    public boolean validar(Fmd fmd2,byte[] pattern){
        try {
            System.out.println(pattern.length);
            System.out.println(Charset.defaultCharset().displayName());
            Fmd fmd1 = UareUGlobal.GetImporter().ImportFmd(pattern, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
            int vcomp = engine.Compare(fmd2, 0, fmd1, 0);
            System.out.println("Parecido de huella: "+vcomp);
            return vcomp<2000;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Compara una huella con varios patrones
     * @param patterns
     * @return Si la huella coincide con alguno de los patrones
     */
    public boolean validar(byte[][]patterns){
        Fmd fmd2 = capturar();
        for(byte[]patern:patterns)
            if(validar(fmd2,patern))
                    return true;
        return false;
    }
    
    public int validar1N(Fmd[]patterns) throws UareUException{
        
        Fmd huella = capturar();
        
        long t0 = System.currentTimeMillis();
        System.out.println("Identificando...");
        
        Candidate[] cand = engine.Identify(huella, 0, patterns, 10000, 1);
        
        System.out.println("Tiempo de procesamiento: "+(System.currentTimeMillis()-t0)+"ms");
        
        return cand.length>0 ? cand[0].fmd_index : -1;
    }
    
    public Empleado getEmpleado(List<Empleado> emp){
        try{
            Fmd[]huellas = new Fmd[emp.size()];
        
            for(int i = 0;i<emp.size();i++){
                Empleado e = emp.get(i);
                
                huellas[i] = UareUGlobal.GetImporter().ImportFmd(e.getHuella(), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
            }
            int n=validar1N(huellas);
            if(n>=0){
                return emp.get(n);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compara una huella con varios patrones
     * @param patterns
     * @return Si la huella coincide con alguno de los patrones
     */
    public int validar(Fmd huella,byte[][]patterns){
        for(int i = 0;i<patterns.length;i++)
            if(validar(huella,patterns[i]))
                    return i;
        return -1;
    }
    
    public Fmd capturar(){
        Fmd f = null;
        try {
            r.Close();
        } catch (Exception e) {
        }
        try {
            r.Open(Reader.Priority.EXCLUSIVE);
            Fid imagen = null;
            while(imagen == null){
                imagen = r.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT,  500, -1).image;
                System.out.println("Imagen: "+imagen);
            }
            f = engine.CreateFmd(imagen, Fmd.Format.ANSI_378_2004);
            r.Close();
        } catch (UareUException ex) {
            ex.printStackTrace();
        }
        return f;
    }
    
    
    public static void main(String args[]){
        try {
            Lector l = new Lector();
        } catch (UareUException ex) {
            Logger.getLogger(Lector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(Lector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
