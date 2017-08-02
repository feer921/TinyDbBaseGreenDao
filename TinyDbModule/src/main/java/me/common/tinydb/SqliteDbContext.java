package me.common.tinydb;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;


/**
 * ******************(^_^)***********************
 * User: 11776610771@qq.com
 * Date: 2017/4/20
 * Time: 10:38
 * DESC: 提供创建SqliteDatabase数据库的上下文对象，可用来创建SD卡路径下的数据库
 * ******************(^_^)***********************
 */

public class SqliteDbContext extends ContextWrapper {
    private static final String TAG = "SqliteDbContext";
    /**
     * 存储Db数据库文件的目录File对象,即DB文件的所在文件夹路径
     */
    protected File dbFileDir;
    public SqliteDbContext(Context baseContext,File dbFileDir) {
        super(baseContext);
        this.dbFileDir = dbFileDir;
    }

    @Override
    public File getDatabasePath(String dbName) {
        if (dbFileDir == null) {
            return super.getDatabasePath(dbName);
        }
        File dbFile = new File(dbFileDir, dbName);
        Log.i(TAG, "-->getDatabasePath() dbFile = " + dbFile);
        return  dbFile;
    }

    /**
     * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法
     *
     * @param name
     * @param mode
     * @param factory
     * @return
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
    }

    /**
     * Android 4.0会调用此方法获取数据库。
     *
     * @param name
     * @param mode
     * @param factory
     * @param errorHandler
     * @return
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        File dbFile = getDatabasePath(name);
        return SQLiteDatabase.openOrCreateDatabase(dbFile, factory);
    }
}
