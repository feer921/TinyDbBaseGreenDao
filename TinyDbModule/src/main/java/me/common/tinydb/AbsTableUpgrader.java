package me.common.tinydb;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * ******************(^_^)***********************<br>
 * User: 11776610771@qq.com<br>
 * Date: 2017/5/15<br>
 * Time: 16:43<br>
 * <P>DESC: 升级数据库时需要处理表更新任务的表升级者
 * </p>
 * ******************(^_^)***********************
 */

public abstract class AbsTableUpgrader {
    protected final String TAG = getClass().getSimpleName();
    protected String tableName;
    protected int newVersion;
    protected int oldVersion;
    protected SQLiteDatabase db;//或者使用SQLiteDatabase代表更通用一点

    /**
     *
     * @param db
     * @param newVersion
     * @param oldVersion
     * @param tableName 请保持该参数为非空
     */
    public AbsTableUpgrader(SQLiteDatabase db, int newVersion, int oldVersion,String tableName) {
        this.tableName = tableName;
        this.newVersion = newVersion;
        this.oldVersion = oldVersion;
        this.db = db;
    }

    private AbsTableUpgrader() {

    }


//    public void createMyTable(SQLiteDatabase db, boolean isNotExists) {
//
//    }

    /**
     * 升级表，思路分为四步
     * 1、重命名原来的表
     * 2、再创建一次原表
     * 3、从临时表中把数据给Load进新建的表
     * 4、删除掉生成的临时表
     */
    public void upgradeTables() {
        if (db == null || !isNeedUpgradeDueToVersionCases()) {
            return;
        }
        try {
            db.beginTransaction();
            //1、重命名原来的表
            String tempTableName = tableName + "_temp";
            String sql = "ALTER TABLE " + tableName +" RENAME TO " + tempTableName;
            db.execSQL(sql);
            //2、再创建一次原表，这里可以传入false因为上面已经把原表重命名了
            createMyTable(false);
            //3、准备从临时表中把数据给Load进新建的表的前提：获取需要复制数据的所有字段
            ArrayList<String> toInsertDataColumns = getNeedLoadDataColumnsFromTempTable(getIgnoreCopyDataColumns());
            //4、使用SQL语句将数据从被重命名的旧表中复制时新建表
            if (toInsertDataColumns != null && toInsertDataColumns.size() > 0) {
                String columnsInsertSql = sqlInsertData2Columns(toInsertDataColumns);
                sql =   "INSERT INTO " + tableName +
                        " (" + columnsInsertSql + ") " +
                        " SELECT " + columnsInsertSql + " FROM " + tempTableName;
                db.execSQL(sql);
            }
            //5、删除掉生成的临时表
            db.execSQL("DROP TABLE IF EXISTS " + tempTableName);
            db.setTransactionSuccessful();
        } catch (Exception ignore) {
            Log.e(TAG, "--> upgradeTables() occur " + ignore);
//            db.execSQL("DROP TABLE IF EXISTS " + tableName + "_temp");
//            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * 根据版本号，获取对应版本时的新增或者修改的字段(列名)
     * 以备在从旧表中复制数据到新表时需要忽略的字段集时使用<BR>
     * 注：调用于：{@link #getIgnoreCopyDataColumns()}
     * @return 所传入参数theVersion对应版本时的新增、修改的字段集合
     */
    protected abstract ArrayList<String> newOrModifiedColumnsBaseVersion(int theVersion);
    /**
     * 在关注市场上留存的旧版本情况下，根据当前数据库版本判断是否需要升级
     * 由于本APK打包发布的情况下，数据库的版本始终是最新的，所以代码也应该是根据最新的数据库版本来判断，旧的代码就可以废除了
     * 比如：现在要发布出去的版本为1.3，对应数据库版本为7，则只需按7来判断并且关注市场内留存的旧APK的版本就行
     * 注：本方法用来判断是否需要进行升级表逻辑操作(重命名旧表-->重新建新表-->从重命名的旧表中复制数据到新表中)
     * <P>invoke in {@link #upgradeTables()}</P>
     * @return
     */
    protected abstract boolean isNeedUpgradeDueToVersionCases();

    /**
     * 根据当前表，以及当前的新数据库版本，来获取当前表中不需要从生成的临时表中复制数据出来的列名、字段
     * 注：因为当前表（即newVersion时的表）中有些字段可能在临时表(老表)中不存在，或者当前表中已经改名(这个暂时不知道怎么处理)
     * @param ignoreCopyDataColumns 因为当前表（即newVersion时的表）中有些字段可能在临时表(老表)中不存在，或者当前表中已经改名(这个暂时不知道怎么处理),所以需要忽略
     * @return 需要从老表中复制数据的列名、字段
     */
    protected ArrayList<String> getNeedLoadDataColumnsFromTempTable(ArrayList<String> ignoreCopyDataColumns){
        List<String> result = null;
        String[] totalWholeColumns = getColumnsNames(tableName);
        if (totalWholeColumns != null && totalWholeColumns.length > 0) {
            if (ignoreCopyDataColumns == null || ignoreCopyDataColumns.isEmpty()) {
                result = Arrays.asList(totalWholeColumns);
            }
            else{//有要忽略的列名/字段
                for (String theColumn : totalWholeColumns) {
                    if (ignoreCopyDataColumns.contains(theColumn)) {
                        continue;
                    }
                    if (result == null) {
                        result = new ArrayList<>(totalWholeColumns.length);
                    }
                    result.add(theColumn);
                }
            }
        }
        return (ArrayList<String>) result;
    }
    /**
     * 根据表名，以及当前的新数据库版本，来获取对应的表中不需要(不能)从生成的临时表中复制数据出来的列名、字段
     * 注：目前该方法已经可以统一、通用，子类可不重写，通用依据为：假设当前数据库版本为X，升级到新表X+N时，X版本的表中所没有的字段为相隔N个版本之间所
     * 作的新增、修改的字段，则这N版本期间所有字段需要忽略(不能)从旧表中复制出数据
     * @return 不需要从临时表中复制数据的列名、字段(升级表时，一些新定的字段、列名在原表中可能会没有，所以需要忽略)
     */
    public ArrayList<String> getIgnoreCopyDataColumns(){
        ArrayList<String> ignoreCopyDataColumns = null;
        for (int i = oldVersion + 1; i <= newVersion; i++) {//从之前旧版本到要升级的新版本时中间有增加/修改的字段数据以加入忽略集合
            ArrayList<String> newOrModifiedColumns = newOrModifiedColumnsBaseVersion(i);
            if (null == newOrModifiedColumns) {
                continue;
            }
            if (ignoreCopyDataColumns == null) {
                ignoreCopyDataColumns = new ArrayList<>();
            }
            ignoreCopyDataColumns.addAll(newOrModifiedColumns);
        }
        return ignoreCopyDataColumns;
    }

    /**
     * 重新创建表
     * 于{@link #upgradeTables()}中调用
     * @param ifNotExists 创建表时判断表是否已经存在，如果旧表存在的话，直接创建表会失败；true:要判断是否存在；false:不判断表是否存在
     */
    public abstract void createMyTable(boolean ifNotExists);

    /**
     * 从一张表中获取到所有的列名、字段
     * @param tableName 表名
     * @return 表中的所有列名、字段
     */
    public String[] getColumnsNames(String tableName) {
        String[] columnNames = null;
        Cursor c = null;
        try
        {
            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (null != c)
            {
                int columnIndex = c.getColumnIndex("name");
                if (-1 == columnIndex)
                {
                    return null;
                }

                int index = 0;
                columnNames = new String[c.getCount()];
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
                {
                    columnNames[index] = c.getString(columnIndex);
                    index++;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (c != null) {
                c.close();
            }
        }
        return columnNames;
    }
    /**
     * 将列名、字段，编写成sql语句需要的格式
     * eg.:列名有{"name","sex","age"},则在要插入数据的sql语句中要变为
     * name,sex,age,参见插入语句 :insert into tableName(name,sex,age) values("","","");
     * 或者 insert into tableName(name,sex,age) select name,sex,age from otherTableName;
     * @param toInsertDataColumns
     * @return
     */
    protected String sqlInsertData2Columns(ArrayList<String> toInsertDataColumns) {
        String sqlColumns = "";
        if (toInsertDataColumns != null && !toInsertDataColumns.isEmpty()) {
            int size = toInsertDataColumns.size();
            for(int i = 0;i < size; i++) {
                sqlColumns += toInsertDataColumns.get(i);
                if (i < size - 1) {//不是最后一个时，后面再追加","
                    sqlColumns += ",";
                }
            }
        }
        return sqlColumns;
    }

    protected void dropTable(String tableName) {
        if (db == null) {
            return;
        }
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }
}
