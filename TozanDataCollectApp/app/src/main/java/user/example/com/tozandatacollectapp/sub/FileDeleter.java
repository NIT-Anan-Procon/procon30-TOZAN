package user.example.com.tozandatacollectapp.sub;

import java.io.File;

public class FileDeleter {
    public static void deleteFile(File file){
        if(!file.exists()) return;
        if(file.list() != null && file.list().length > 0) {
            for (String child : file.list()) {
                deleteFile(new File(file, child));
            }
        }
        file.delete();

    }
}
