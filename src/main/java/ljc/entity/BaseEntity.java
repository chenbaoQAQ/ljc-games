package ljc.entity;

import lombok.Data;

/**
 * BaseEntity: 所有战斗单位的父类
 */
@Data
public class BaseEntity {
    protected String name;       // protected 关键字让子类可以直接访问
    protected Integer level;
    protected Integer maxHp;
    protected Integer currentHp;
    protected Integer baseAtk;
}