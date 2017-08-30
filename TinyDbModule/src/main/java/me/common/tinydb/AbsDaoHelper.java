package me.common.tinydb;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * ******************(^_^)***********************<br>
 * User: 11776610771@qq.com<br>
 * Date: 2017/5/6<br>
 * Time: 10:14<br>
 * DESC: 通用的Dao helper,提供一些通用的表的CRUD；一般把子类写成单例模式并根据业务需求扩展自身来使用
 * T:为数据对象类型,对应一张表结构；I为本类自身（方便链式调用，在本类作用不大）；PK:指代AbstractDao中所对应的某表的Primary key类型<br>
 * ******************(^_^)***********************<br>
 */

public abstract class AbsDaoHelper<T, I extends AbsDaoHelper<T, I,PK>,PK> {
    protected final String TAG = getClass().getSimpleName();
    protected AbstractDao<T, PK> curDao;//这里的Long类型指定了Dao的Primary key o Long 类型，一般的表的Primary key 为"_id"字段
    protected AbsDaoHelper(){
        curDao = provideDao();
    }
    protected abstract AbstractDao<T, PK> provideDao();
    /**
     * 往数据库的表中添加一条数据
     * @param data 要添加的数据
     * @return row id
     */
    public long addData(T data) {
        checkDaoNotNull();
        return curDao.insertOrReplace(data);
    }

    public I addData(T... datas) {
        checkDaoNotNull();
        curDao.insertOrReplaceInTx(datas);
        return self();
    }
    public List<T> getAllData() {
        checkDaoNotNull();
        return curDao.loadAll();
    }

    public long getTotalCount() {
        checkDaoNotNull();
        return queryBuilder().buildCount().count();
    }
    public T getDataByRowId(long rowId) {
        if (rowId < 0) {
            return null;
        }
        checkDaoNotNull();
        return curDao.loadByRowId(rowId);
    }

    /**
     * 根据 表的primaryKey的值来查找某条数据
     * @param primaryKey Primary key 对应的值,一般来讲一张表的Primary key为"_id"字段
     * @return
     */
    public T getDataByPkId(PK primaryKey) {
        checkDaoNotNull();
        return curDao.load(primaryKey);
    }

    public I deleteDataByPk(PK pk) {
        checkDaoNotNull();
        curDao.deleteByKey(pk);
        return self();
    }

    public I deleteDataByPk(PK... pks) {
        checkDaoNotNull();
        curDao.deleteByKeyInTx(pks);
        return self();
    }
    public I deleteData(T data) {
        checkDaoNotNull();
        curDao.delete(data);
        return self();
    }

    /**
     * 使用事务批量删除指定的数据
     * @param datas 要删除的数据
     * @return
     */
    public I deleteData(T... datas) {
        checkDaoNotNull();
        curDao.deleteInTx(datas);
        return self();
    }

    public I deleteAll() {
        checkDaoNotNull();
        curDao.deleteAll();
        return self();
    }

    public I update(T data) {
        if (data != null) {
            checkDaoNotNull();
            curDao.update(data);
        }
        return self();
    }
    public QueryBuilder<T> queryBuilder() {
        checkDaoNotNull();
        return curDao.queryBuilder();
    }
    void checkDaoNotNull() {
        if (curDao == null) {
            curDao = provideDao();
        }
        if (curDao == null) {
            throw new NullPointerException(" sorry! cur dao is null,can't excute...");
        }
    }

    public AbstractDao<T,PK> getDao() {
        return curDao;
    }
    protected I self() {
        return (I) this;
    }
}
