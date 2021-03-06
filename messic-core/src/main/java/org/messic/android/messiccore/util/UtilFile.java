/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.messiccore.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;

public class UtilFile {
    public static final String DEFAULT_REPLACEMENT_CHAR = "_";

    /**
     * All the illegal characters for a filename or a path (very restricive due to the fact of fat32 SD cards
     */
    public static final String ILLEGAL_FILENAME_CHARS = "[\\]\\[!\"#$%&()*+,/:;<=>?@\\^`{|}~]+";

    public static final Pattern ILLEGAL_FILENAME_CHARS_PATTERN = Pattern.compile(ILLEGAL_FILENAME_CHARS);

    public static final String MESSIC_FOLDER = "/messic";

    public static final String DESTINATION_FOLDER = MESSIC_FOLDER + "/offline";

    /**
     * .nomedia is a file that says to the gallery app that don't use that folder to show music or pictures from there...
     * If we don't put this, the folder, with covers and so on, is shown in the gallery app and others.
     */
    public static final String NO_MEDIA_FILE = ".nomedia";

    /**
     * Return a valid location to a file path
     *
     * @param filename             {@link String} location to convert
     * @return a valid location
     */
    public static String replaceIllegalFilenameCharacters(String filename) {
        Matcher matcher = ILLEGAL_FILENAME_CHARS_PATTERN.matcher(filename);
        String result = matcher.replaceAll(DEFAULT_REPLACEMENT_CHAR);
        return result;
    }

    /**
     * Save the content of an inputstream to a file at the filesystem
     *
     * @param fDestination
     * @param inputStream
     * @throws IOException
     */
    public static void saveToFile(File fDestination, InputStream inputStream)
            throws IOException {
        // this dynamically extends to take the bytes you read
        FileOutputStream byteBuffer = new FileOutputStream(fDestination);

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        byteBuffer.close();
        inputStream.close();
    }

    public static String getMessicRootFolderAbsolutePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + MESSIC_FOLDER;
    }

    public static String getMessicOfflineFolderAbsolutePath() {
        setNoMediaFolder();
        return Environment.getExternalStorageDirectory().getAbsolutePath() + DESTINATION_FOLDER;
    }

    /**
     * .nomedia is a file that says to the gallery app that don't use that folder to show music or pictures from there...
     * If we don't put this, the folder, with covers and so on, is shown in the gallery app and others.
     */
    public static void setNoMediaFolder() {
        String messicFolder = getMessicRootFolderAbsolutePath();
        File fmessicFolder = new File(messicFolder);
        if (!fmessicFolder.exists()) {
            fmessicFolder.mkdirs();
        }

        String nomediaFile = messicFolder + "/" + NO_MEDIA_FILE;
        File fnomedia = new File(nomediaFile);
        if (!fnomedia.exists()) {
            try {
                fnomedia.createNewFile();
            } catch (Exception e) {
                Log.w("UtilFile", ".nomedia file cannot be created!!");
                e.printStackTrace();
            }
        }

    }

    /**
     * Remove a directory and its content
     *
     * @param path {@link File} path to remove
     * @return
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Remove the messic folder and its content
     */
    public static void emptyMessicFolder() {
        File fmessicfolder = new File(UtilFile.getMessicOfflineFolderAbsolutePath());
        UtilFile.deleteDirectory(fmessicfolder);
    }
}
