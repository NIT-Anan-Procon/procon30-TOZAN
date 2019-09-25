package user.example.com.tozandatacollectapp.sub;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.ArraySet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StorageAcquirer {

    private StorageAcquirer(){

    }

    private static StorageAcquirer storageAcquirer = new StorageAcquirer();


    private Set<String>
            internalStoragePaths, externalStoragePaths;

    private Context c;

    public static void init(Activity c){
        storageAcquirer.c = c;
        storageAcquirer.getSdCardFilesDirPathList(c);
    }

    public static StorageAcquirer getInstance(){
        return storageAcquirer;
    }

    public void reacquire(){
        getSdCardFilesDirPathList(c);
    }

    private void getSdCardFilesDirPathList(Context context) {

        internalStoragePaths = new ArraySet<>();
        externalStoragePaths = new ArraySet<>();

        // getExternalFilesDirsはAndroid4.4から利用できるAPI。
        // filesディレクトリのリストを取得できる。
        File[] dirArr = context.getExternalFilesDirs(null);

        for (File dir : dirArr) {
            if (dir != null) {
                String path = dir.getAbsolutePath();

                // isExternalStorageRemovableはAndroid5.0から利用できるAPI。
                // 取り外し可能かどうか（SDカードかどうか）を判定している。
                if (Environment.isExternalStorageRemovable(dir)) {

                    // 取り外し可能であればSDカード。
                    if (!externalStoragePaths.contains(path)) {
                        externalStoragePaths.add(path);
                    }

                } else {
                    // 取り外し不可能であれば内部ストレージ。
                    if (!internalStoragePaths.contains(path)) {
                        internalStoragePaths.add(path);
                    }
                }
            }
        }
    }

    public List<String> getExternalStorageList() {
        return new ArrayList<>(externalStoragePaths);
    }

    public List<String> getInternalStorageList() {
        return new ArrayList<>(internalStoragePaths);
    }

    public List<String> getAllStorageList(){
        List<String> value = new ArrayList<>();
        value.addAll(internalStoragePaths);
        value.addAll(externalStoragePaths);
        return value;
    }

    public static String toStorageRootPath(String path){
        return path.replaceAll("/Android/.*$", "");
    }

    public static List<String> toStorageRootPath(List<String> path){
        List<String> value = new ArrayList<>();

        //フォルダパスのうちの書き込み可能なストレージのルート部分を抜き出す。
        //例:/storage/emulated/0/Android/data/user.example.com.tozandatacollectapp/filesなら
        // /Android 以下を消去した, /storage/emulated/0 となる
        for(String str : path) value.add(toStorageRootPath(str));
        return path;
    }
}
