/*
 * OlympicDate.java
 *
 * Created on February 8, 2006, 5:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author magnuse
 */
public class OlympicData {
    
    private static final String PARTICIPANTS_FILE = "olympic_games_participants.csv";
    private static final String DICIPLINES_FILE  = "olympic_games_diciplines.csv";
    private static final String TERMS_FILE = "olympic_terms.txt";
    
    private static Map participants;
    private static Map diciplines;
    private static Map participantsPerDicipline = new HashMap();
    private static Map terms;
    
    private static OlympicData instance;
    
    static Locale locale = new Locale("no", "NO");
    
    private OlympicData() {
    }
    
    public static OlympicData instance() {
        if (instance == null) {
            instance = new OlympicData();
        }
        
        return instance;
    }
    
    public Map getParticipants() {
        if (participants == null) {
            participants = new HashMap();
            processResourceFile(PARTICIPANTS_FILE, new ParticipantLineHandler());
        }
        return participants;
    }
    
    public Map getDiciplines() {
        if (diciplines == null) {
            diciplines = new HashMap();
            processResourceFile(DICIPLINES_FILE, new DiciplineLineHandler());
        }
        return diciplines;
    }

    public Map getTerms() {
        if (terms == null) {
            terms = new HashMap();
            processResourceFile(TERMS_FILE, new TermsLineHandler());
        }
        return terms;
    }
    
    public Map getParticipantsPerDicipline() {
        return participantsPerDicipline;
    }
    
    private void processResourceFile(String resourceName, LineHandler lineHandler) {
        try {
            InputStream is = getClass().getResourceAsStream("/" + resourceName);
            InputStreamReader ir = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new BufferedReader(ir);
            
            String line;
            
            reader.readLine(); // Discard first line.
            
            while ((line = reader.readLine()) != null) {
                lineHandler.processLine(line);
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public interface LineHandler {
        void processLine(String line);
    }
    
    private class ParticipantLineHandler implements LineHandler {
        public void processLine(String line) {
            try {
                
                String[] fields = line.split(";");
                
                String diciplineNmae = fields[1].trim().toLowerCase(locale);
                String participantName = fields[0].trim().toLowerCase(locale);
                Map map = new HashMap();
                map.put("diciplineName", diciplineNmae);
                if (fields.length > 2) {
                    map.put("infoLink1", fields[2].trim());
                }
                if (fields.length > 3) {
                    map.put("infoLink2", fields[3].trim());
                }
                if (fields.length > 4) {
                    map.put("infoLink3", fields[4].trim());
                }
                
                participants.put(participantName, map);
                ArrayList parts = (ArrayList) participantsPerDicipline.get(diciplineNmae);
                
                if (parts == null) {
                    parts = new ArrayList();
                    participantsPerDicipline.put(diciplineNmae, parts);
                }
                
                parts.add(participantName);
                
            } catch (Exception e) {
                System.err.println("Unable to process line " + line);
            }
        }
    }
    
    private class DiciplineLineHandler implements LineHandler {
        public void processLine(String line) {
            String[] fields = line.split(";");
            Map map = new HashMap();
            map.put("tvLink", fields[2].trim());
            map.put("programLink", fields[1].trim());
            if (fields.length > 3) {
                map.put("alternativePicSearch", fields[3].trim());
            }
            diciplines.put(fields[0].trim().toLowerCase(locale), map);
        }
    }
    
    private class TermsLineHandler implements LineHandler {
        public void processLine(String line) {
            terms.put(line.trim().toLowerCase(locale), "1");
        }
    }
}
