package me.tinydb.dbAndDao;

import org.greenrobot.greendao.AbstractDao;

import me.common.tinydb.AbsDaoHelper;
import me.tinydb.demo.User;

/**
 * ******************(^_^)***********************<br>
 * User: 11776610771@qq.com<br>
 * Date: 2017/8/30<br>
 * Time: 15:30<br>
 * <P>DESC:
 * </p>
 * ******************(^_^)***********************
 */

public class UserDataHelper extends AbsDaoHelper<User, UserDataHelper, Long> {


    private UserDataHelper() {
        //一般本类 为单例使用，构造方法私有化
    }

    @Override
    protected AbstractDao<User, Long> provideDao() {
        return null;
    }
}
