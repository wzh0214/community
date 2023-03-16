package com.wzh.community.entity;

/**
 * @author wzh
 * @data 2022/7/30 -15:28
 *
 * 封装分页相关的信息
 */
public class Page {
    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数(用于计算页数)
    private int rows;
    // 查询路径(用于复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        // 判断一下，防止用户传小于等于0的页码数
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit > 0 && limit < 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    /**
     * 获取当前页的起始页
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总的页数
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
        //return rows % limit == 0 ? rows / limit : rows / limit + 1;
    }

    /**
     * 获取起始页码
     */
    public int getFrom() {
        int from = current - 2;
        return from > 0 ? from : 1;

    }

    /**
     * 获取结束页码
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return total > to ? to : total;
    }





}
