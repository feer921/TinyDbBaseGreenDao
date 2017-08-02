package me.tinydb.demo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * ******************(^_^)***********************<br>
 * User: 11776610771@qq.com<br>
 * Date: 2017/8/2<br>
 * Time: 19:15<br>
 * <P>DESC:
 * </p>
 * ******************(^_^)***********************
 */
@Entity
public class User {
    @Id(autoincrement = true)
    private Long id;
    private String sex;
    private String name;
    @Generated(hash = 1614371365)
    public User(Long id, String sex, String name) {
        this.id = id;
        this.sex = sex;
        this.name = name;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSex() {
        return this.sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
