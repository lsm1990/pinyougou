package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车接口
 * @author lsm
 */
public interface CartService {
    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    /**
     * 由redis查找购物车列表
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 添加购物车列表到redis
     * @param username
     * @param cartList
     */
    public void addCartListToRedis(String username,List<Cart> cartList);

    /**
     * 登录后合并购物车
     * @param cartListCookie
     * @param cartListRedis
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartListCookie,List<Cart> cartListRedis);

}
