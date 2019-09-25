package user.example.com.tozandatacollectapp.sub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipManager {

    public interface ProgressCallback{
        void onCollectFileFinish(int max);
        void onProgressChange(int progress, File file);
        void onZipFinish(File out);
        void onError(Exception e);
    }

    public static void zip(File in, File out, ProgressCallback progressCallback){

        if(!in.exists()) throw new IllegalArgumentException("入力元のファイル(" + in.getAbsolutePath() + ")が存在しない");

        String parent = in.getParent() + "/";
        List<File> srcList = new ArrayList<>();
        collectFiles(in, srcList);
        int max = srcList.size(), min = 0;
        progressCallback.onCollectFileFinish(max);

        //バッファ
        byte[] buffer = new byte[1024];

        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out));
            zos.setLevel(9);

            for(File file : srcList){
                String str = file.getAbsolutePath().replace(parent, "");
                if(file.isDirectory()){
                    zos.putNextEntry(new ZipEntry(str+"/"));
                }else{
                    InputStream is = new FileInputStream(file);

                    ZipEntry entry = new ZipEntry(str);
                    entry.setMethod(ZipEntry.DEFLATED);
                    zos.putNextEntry(entry);

                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }

                    is.close();
                }
                zos.closeEntry();
                progressCallback.onProgressChange(++min, file);
            }

            zos.close();
            progressCallback.onZipFinish(out);
        } catch (Exception e) {
            progressCallback.onError(e);
        }
    }

    private static void collectFiles(File parent, List<File> srcList){
        srcList.add(parent);
        if(!parent.isDirectory()) return;

        for(File child : parent.listFiles()){
            collectFiles(child, srcList);
        }
    }
}
