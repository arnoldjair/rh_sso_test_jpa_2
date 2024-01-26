package com.gorgosoft.jpa.user;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(name = "getUserByUsername", query = "select u from UserEntity u where u.username = :username"),
        @NamedQuery(name = "getUserByEmail", query = "select u from UserEntity u where u.email = :email"),
        @NamedQuery(name = "getUserCount", query = "select count(u) from UserEntity u"),
        @NamedQuery(name = "getAllUsers", query = "select u from UserEntity u"),
        @NamedQuery(name = "searchForUser", query = "select u from UserEntity u where " +
                "( lower(u.username) like :search or u.email like :search ) order by u.username"),
})
@Entity(name = "UserEntity")
@Data
public class UserEntity {

    @Id
    private String id;


    private String username;
    private String email;
    private String password;
    private String name;

}
