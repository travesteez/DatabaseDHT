/***************************************************************************
 *                                                                         *
 *                           RetrieveNetwork.java                          *
 *                            -------------------                          *
 *   date                 : 15.09.2004                                     *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                          karsten.loesing@uni-bamberg.de                 *
 *                                                                         *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   A copy of the license can be found in the license.txt file supplied   *
 *   with this software or at: http://www.gnu.org/copyleft/gpl.html        *
 *                                                                         *
 ***************************************************************************/

package de.uniba.wiai.lspi.chord.console.command;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;

import java.io.Serializable;
import java.io.*;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;

/**
 * <p>
 * {@link Command} to retrieve a value from the remote chord network.
 * </p>
 * To get a description of this command type <code>retrieveN -help</code>
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * @author  sven
 * @version 1.0.5
 */
public class RetrieveFile extends Command {

   /**
    * The name of this {@link Command}.
    */
    public static final String COMMAND_NAME = "retrieveFile";

    /**
     * The name of the parameter, that defines the key to be retrieved.
     */
    protected static final String KEY_PARAM = "key";

    /** Creates a new instance of Retrieve
     * @param toCommand1
     * @param out1 */
    public RetrieveFile(Object[] toCommand1, java.io.PrintStream out1) {
        super(toCommand1, out1);
    }

    public void exec() throws ConsoleException {
        String key = this.parameters.get(KEY_PARAM);
        if ( (key == null) || (key.length() == 0) ){
            throw new ConsoleException("Not enough parameters! " + KEY_PARAM + " is missing.");
        }

        try {
            FileOutputStream fos = new FileOutputStream(key);

            Key keyObject = new Key(key);

            Chord chord = ((RemoteChordNetworkAccess)this.toCommand[1]).getChordInstance();
            try {
                Set<Serializable> vs = chord.retrieve(keyObject);
                Object[] values = vs.toArray(new Object[vs.size()]);

                String numChunksStr = (String) values[0];
                Long numChunks = Long.parseLong(values[0]);

                key += ".000000";
                for (int i = 0; i < numChunks;) {
                    keyObject = new Key(key);
                    vs = chord.retrieve(keyObject);
                    values = vs.toArray(new Object[vs.size()]);
                    String hexBytes = values[0];
                    
                    byte[] bytes = DatatypeConverter.parseHexBinary(hexBytes);

                    fos.write(bytes);

                    key = key.replace(key.substring(key.length()-6), String.format("%06d", ++i));
                }
                fos.close();
            }
            catch (Throwable t){
                ConsoleException e = new ConsoleException("Exception during execution of command. " + t.getMessage());
                e.setStackTrace(t.getStackTrace());
                throw e;
            }
        }
        catch (Exception e) {
            this.out.println("Got exception when retrieving file.");
        }

    }

    public String getCommandName() {
        return COMMAND_NAME;
    }

    public void printOutHelp() {
        this.out.println("This command retrieves and displays the values stored for a provided key in the chord network.");
        this.out.println("The search is initiated by the node provided as parameter.");
        this.out.println("Required parameters: ");
        this.out.println("\t" + KEY_PARAM + ": The key for the values.");
        this.out.println();
    }

}
